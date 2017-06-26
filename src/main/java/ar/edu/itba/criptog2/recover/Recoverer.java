package ar.edu.itba.criptog2.recover;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import ar.edu.itba.criptog2.util.BmpWriter;
import ar.edu.itba.criptog2.util.LagrangeInterpolator;
import ar.edu.itba.criptog2.util.Polynomial;
import net.sourceforge.argparse4j.inf.Namespace;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Recoverer implements Worker {

	private List<BmpParser> pictures = new ArrayList<>();

	private int k;

	private byte[] secretPicture;

	private String secretFilePath;

	private Recoverer() {}

	public static Recoverer createFromNamespace(final Namespace ns) {

		final Recoverer recoverer = new Recoverer();

		recoverer.k = ns.getInt("k");
		if(recoverer.k <= 1){
			System.err.println("K should be greater than or equal to 2.");
			System.err.println("Aborting.");
			System.exit(1);
		}
		recoverer.secretFilePath = ns.getString("secret");

		//load picture files from path
		final File[] files = new File(ns.getString("dir")).listFiles();
		if(files == null) {
			System.err.println(ns.getString("dir") + " is not a directory or it does not exist. Aborting.");
			System.exit(1);
		}
		if(files.length < recoverer.k){
			System.err.println("There should be at least K files in the given directory");
			System.err.println("Aborting.");
			System.exit(1);
		}
		int shadowSize = 0;
		for (int i = 0; i < recoverer.k; i++) {
			try {
				if(files[i].isFile() && files[i].getName().endsWith(".bmp")){
					BmpParser bmpParser = new BmpParser(files[i].getPath());
					recoverer.pictures.add(bmpParser);
					if(shadowSize == 0){
						shadowSize = bmpParser.getWidth() * bmpParser.getHeight();
					}else if(shadowSize != files[i].getTotalSpace()){
						System.err.println("All shadow images should have the same size");
						System.err.println("Aborting.");
						System.exit(1);
					}
				}else{
					i--;
				}
			} catch (IOException e) {
				System.err.println("Error opening shadows: " + e.getMessage());
				System.err.println("Aborting.");
				System.exit(1);
			}
		}
		return recoverer;
	}
	
	@Override
	public void work() {
		List<Point> points;
		LagrangeInterpolator lagrangeInterpolator = new LagrangeInterpolator();
		int byteCount = 0;
		int[] coeffs;
		int height = k == 8 ? pictures.get(0).getHeight() : pictures.get(0).getSecretHeight();
		int width = k == 8 ? pictures.get(0).getWidth() : pictures.get(0).getSecretWidth();
		int secretPictureSize = width * height;
		secretPicture = new byte[secretPictureSize];
		int j = 0;

		while(byteCount < secretPictureSize && (j+1)*k < secretPictureSize) {
			//Step 1: Get the first eight bytes of each picture
			points = getPoints(j);

			//Step 2: Find polynomial
			Polynomial polynomial = lagrangeInterpolator.interpolate(points,257);
			coeffs = polynomial.getCoefficients();

			//Step 3: Build piece of secret picture
			//When the coefficient of the largest degree of the polynomial is 0, it is written.
            for (int i = coeffs.length; i < k ; i++) {
                secretPicture[byteCount++] = 0;
            }
            for(int c : coeffs){
				secretPicture[byteCount++] = (byte)c;
			}
			j++;
		}

		//Step 5: Reorder image
		revealSecret(pictures.get(0).getSeed());

		//Write secret picture
		BmpWriter bmpWriter = new BmpWriter.BmpWriterBuilder()
				.file(new File(secretFilePath))
				.width(width)
				.height(height)
				.pictureData(secretPicture)
				.build();
		try {
			bmpWriter.writeImage();
		} catch (IOException e) {
			System.err.println("Error writing revealed secret: " + e.getMessage());
			System.err.println("Aborting.");
			System.exit(1);
		}
	}

	/**
	 * Gets an (x, y) point where the secret's polynomial was evaluated, for each shadow. X corresponds to the shadow
	 * number, and Y is distributed along each section of each shadow.
	 *
	 * @param j The section number to get points for.  Will scan the same section of each shadow.
	 * @return The extracted points.
	 */
	private List<Point> getPoints(int j){
		List<Point> points = new ArrayList<>();
		for(BmpParser bmp : pictures) {
			points.add(new Point(bmp.getShadowNumber(), getHiddenByte(bmp, j)));
		}
		return points;
	}

	/**
	 * Cycles through 8 bytes of a specified section of a shadow picture and extracts the least significant bit of each
	 * byte, concatenating them together to obtain a new full byte.
	 *
	 * @param bmp The shadow to traverse.
	 * @param j The section of the shadow to traverse. Each section is 8 bytes long.
	 * @return The obtained byte, as an int.
	 */
	private int getHiddenByte(BmpParser bmp, int j) {
		byte[] picData = bmp.getPictureData();
		StringBuilder byteStr = new StringBuilder();
		for(int i = 0; i < 8; i++) {
			byteStr.append((int) picData[8 * j + i] & 1);
		}
		return Integer.parseInt(byteStr.toString(), 2);
	}

	/**
	 * XORs each byte of the secret picture with a "random" byte from a SEEDED random generator. If the random's seed
	 * is the same used to hide the picture, the XORs applied here will be the same as the ones used to hide the picture,
	 * and thus the secret will be "revealed".
	 *
	 * @param seed The seed to use for the random number generator. Should be the same seed used to hide the picture.
	 */
	private void revealSecret(final int seed) {
		Random rnd = new Random(seed);
		for(int i = 0; i < secretPicture.length; i++) {
			secretPicture[i] = (byte) ((int)secretPicture[i] ^ rnd.nextInt(256));
		}
	}
}
