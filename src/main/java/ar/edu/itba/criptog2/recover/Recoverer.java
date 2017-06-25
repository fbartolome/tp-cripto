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
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class Recoverer implements Worker {

	private List<BmpParser> pictures = new ArrayList<>();

	private int k;

	private byte[] secretPicture;


	private Recoverer() {
		this.k = 8;
		try {
			this.pictures.add(new BmpParser("img/sombras/sombra1.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra2.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra3.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra4.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra5.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra6.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra7.bmp"));
			this.pictures.add(new BmpParser("img/sombras/sombra8.bmp"));

			this.secretPicture = new byte[pictures.get(0).getPictureSize()];
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Recoverer createFromNamespace(final Namespace ns) {
		final Recoverer recoverer = new Recoverer();
//		k = ns.getInt("k");
		// setup recoverer
		
		return recoverer;
	}
	
	@Override
	public void work() {
		List<Point> points;
		LagrangeInterpolator lagrangeInterpolator = new LagrangeInterpolator();
		int byteCount = 0;
		int[] coeffs;

		for(int j = 0; j < pictures.get(0).getPictureSize()/8; j++){
//		paso 1: agarro los primeros 8 bytes de cada foto y consigo un byte por cada una de esas fotos
			points = getPoints(j);

//		paso 2: encuentro el polinomio
			Polynomial polynomial = lagrangeInterpolator.interpolate(points,257);
			coeffs = polynomial.getCoefficients();

//		paso 3: armo el pedacito de imagen del secreto
// 		Hay algunos polinomios que quedan con sus coeficientes de mayor grado en 0. Escribir estos 0s.
            for (int i = coeffs.length; i < 8 ; i++) {
                secretPicture[byteCount++] = 0;
            }
            for(int c : coeffs){
				secretPicture[byteCount++] = (byte)c;
			}
		}

//		paso 5: reordeno los bytes de la imagen secreto
		randomizeTable(pictures.get(0).getSeed());

		BmpWriter bmpWriter = new BmpWriter.BmpWriterBuilder()
				.compressionType(pictures.get(0).getCompressionType()).file(new File("img/secretito.bmp"))
				.height(pictures.get(0).getHeight())
				.width(pictures.get(0).getWidth()).horizontalResolution(pictures.get(0).getHorizontalResolution())
				.verticalResolution(pictures.get(0).getVerticalResolution())
				.numImportantColors(pictures.get(0).getNumImportantColors())
				.numUsedColors(pictures.get(0).getNumUsedColors())
				.pictureData(secretPicture).reservedBytes(pictures.get(0).getReservedBytes())
				.build();
		try {
			bmpWriter.writeImage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Point> getPoints(int j){
		List<Point> points = new ArrayList<>();
		for(BmpParser bmp : pictures){
			points.add(new Point(bmp.getShadowNumber(),getHiddenByte(bmp,j)));
		}
		return points;
	}

	private int getHiddenByte(BmpParser bmp, int j) {
		byte[] picData = bmp.getPictureData();
		String byteStr = "";
		for(int i = 0; i < 8; i++){
			byteStr += ((int)picData[8*j + i] & 1);
		}
		return Integer.parseInt(byteStr, 2);
	}

	private void randomizeTable(final int seed){
		Random rnd = new Random(seed);
		for(int i=0; i < secretPicture.length; i++){
			secretPicture[i] = (byte) ((int)secretPicture[i] ^ rnd.nextInt(256));
		}
	}
}
