package ar.edu.itba.criptog2.distribute;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import ar.edu.itba.criptog2.util.BmpWriter;
import ar.edu.itba.criptog2.util.Polynomial;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Distributor implements Worker {

	private int k;
	private int n;
	private BmpParser secretPicture;
	private Random rnd;
	private List<BmpParser> shadows;
	private int seed;

	private Distributor() {
		this.seed = new Random().nextInt(65536);		//2-byte seed
		this.rnd = new Random(seed);					//Random number generator using that seed
		this.shadows = new ArrayList<>();
	}

	public static Distributor createFromNamespace(final Namespace ns) throws Exception {

		final Distributor distributor = new Distributor();

		distributor.k = ns.getInt("k");
		Optional<Integer> optionalN = Optional.ofNullable(ns.getInt("n"));
		distributor.n = optionalN.orElse(distributor.k);
		boolean providedN = optionalN.isPresent();

		distributor.verifyCorrectKAndCorrectN(providedN);

		try {
			distributor.secretPicture = new BmpParser(ns.getString("secret"));
		} catch (Exception e) {
			System.err.println("Error reading secret picture: " + e.getMessage());
			System.err.println("Aborting.");
			System.exit(1);
		}

		final String shadowDirectory = ns.getString("dir");	// Should never be null
		distributor.loadShadowFiles(shadowDirectory);

		distributor.verifyCorrectAmountOfShadows(providedN);

		return distributor;
	}

	/**
	 * Verify that n if larger than one, that k is larger than 1, and that k is smaller than n
	 * @param providedN
	 */
	private void verifyCorrectKAndCorrectN(boolean providedN){
		if (providedN && this.n <= 1) {
			System.err.println("N should be greater than or equal to 2.");
			System.err.println("Aborting.");
			System.exit(1);
		}
		if (this.k <= 1) {
			System.err.println("K should be greater than or equal to 2.");
			System.err.println("Aborting.");
			System.exit(1);
		}
		if (providedN && this.k > this.n) {
			System.err.println("K should be less than or equal to N.");
			System.err.println("Aborting.");
			System.exit(1);
		}
	}

	/**
	 * Load shadow files to shadows list
	 * @param shadowDirectory from where the shadow files will be loaded
	 */
	private void loadShadowFiles(String shadowDirectory){

		File[] shadowFiles = new File(shadowDirectory).listFiles();
		if(shadowFiles == null) {
			System.err.println(shadowDirectory + " is not a directory or it does not exist. Aborting.");
			System.exit(1);
		}
		int shadowWidth = 0, shadowHeight = 0;
		for (int i = 0; i < shadowFiles.length; i++) {
			if (shadowFiles[i].isFile() && shadowFiles[i].getName().endsWith(".bmp")) {
				BmpParser shadow = null;
				try {
					shadow = new BmpParser(shadowFiles[i].getAbsolutePath());
				} catch (Exception e) {
					System.err.println("Error reading shadow #" + (i+1) +": " + e.getMessage());
					System.err.println("Aborting.");
					System.exit(1);
				}

				//Validate shadow dimensions
				//1) All shadows should have the same dimensions. Store the dimensions of the first encountered shadow and ensure all others have the same dimensions.
				if(i == 0) {
					shadowWidth = shadow.getWidth();
					shadowHeight = shadow.getHeight();
				} else {
					if(shadow.getWidth() != shadowWidth || shadow.getHeight() != shadowHeight) {
						System.err.println("All shadows should have the same dimensions.");
						System.err.println("Aborting.");
						System.exit(1);
					}
				}

				//2) All shadows should have a particular size depending on K
				if (this.k == 8) {
					if (shadow.getWidth() != this.secretPicture.getWidth() || shadow.getHeight() != this.secretPicture.getHeight()) {
						System.err.println("For K = 8, all shadows should have the same dimensions as secret (" + this.secretPicture.getWidth() + "x" + this.secretPicture.getHeight() + "), " + shadowFiles[i].getName() + " is " + shadow.getWidth() + "x" + shadow.getHeight());
						System.err.println("Aborting.");
						System.exit(1);
					}
				} else {
					int secretPixelCount = this.secretPicture.getWidth() * this.secretPicture.getHeight();
					int shadowPixelCount = shadow.getWidth() * shadow.getHeight();
					if(secretPixelCount * (8.0/this.k) > shadowPixelCount) {
						System.err.println("Shadow is not big enough to hide secret");
						System.err.println("Aborting.");
						System.exit(1);
					}
				}

				this.shadows.add(shadow);
			}
		}
	}

	/**
	 * Verify that the amount of shadows specified by parameter coincides with the ones in the chosen directory
	 * @param providedN
	 */
	private void verifyCorrectAmountOfShadows(boolean providedN){
		int numAvailableShadows = this.shadows.size();
		if(numAvailableShadows != this.n) {
			if(providedN)	{
				System.err.println("Specified N=" + this.n + " but there are " + numAvailableShadows + " available shadows, need exactly " + this.n);
				System.err.println("Aborting.");
				System.exit(1);
			} else {
				// Didn't provide N, verify that there are at least K shadows
				if(this.k > numAvailableShadows) {
					System.err.println("Specified K=" + this.k + " but there are " + numAvailableShadows+ " available shadows, need at least " + this.k);
					System.err.println("Aborting.");
					System.exit(1);
				} else {
					// All good, use N = number of available shadows (K <= numAvailableShadows && N == numAvailableShadows ===> K <= N)
					this.n = numAvailableShadows;
				}
			}
		}
	}

	/**
	 * Evaluates the given polynomial in 1, 2, 3, ..., {@link #n} modulo 257.
	 *
	 * @param p The polynomial to evaluate.
	 * @return The evaluations, where the first element is the evaluation in 1.
	 */
	private BigInteger[] makeEvaluations(final Polynomial p) {
		BigInteger[] evaluations = new BigInteger[this.n];

		for (int i = 0; i < this.n; i++) {
			evaluations[i] = p.evaluate(BigInteger.valueOf(i+1)).remainder(BigInteger.valueOf(257));
		}
		return evaluations;
	}

	/**
	 * Checks whether any of the given values is 256. If so, we should re-evaluate the polynomial that generated these values.
	 *
	 * @param evaluations A polynomial's evaluations.
	 * @return Whether the polynomial that created these values should be re-evaluated.
	 */
	private boolean shouldReevaluate(final BigInteger[] evaluations) {
		for (BigInteger evaluation : evaluations) {
			if (evaluation.compareTo(BigInteger.valueOf(256)) == 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void work() {
		final int numberOfPixels = this.secretPicture.getWidth() * this.secretPicture.getHeight();
		final byte[] randomPixels = new byte[numberOfPixels];
		for (int i = 0; i < numberOfPixels; i++) {
			randomPixels[i] = (byte) (this.secretPicture.getPictureData()[i] ^ this.rnd.nextInt(256));
		}

		int j = 0;
		int consumedBytes = 0;

		do {

			// build polynomial
			Polynomial p = new Polynomial(0, 0);
			for (int i = 0; i < this.k; i++) {
				int b = randomPixels[consumedBytes++];
				if(b < 0){
					b += 256;
				}
				p = p.plus(new Polynomial(b, i));
			}

			boolean done = false;
			BigInteger[] evaluations;

			do {

				evaluations = makeEvaluations(p);

				if (shouldReevaluate(evaluations)) {
					for (int i = 0; i < p.getCoefficients().length; i++) {
						// Find the first non-zero coefficient and subtract 1 to it
						BigInteger coef = p.getCoefficientAt(i);
						if (coef.compareTo(BigInteger.valueOf(0)) != 0) {
							p.alterCoefficientAt(i, coef.subtract(BigInteger.valueOf(1)));
							break;
						}
					}
				} else {
					done = true;
				}

			} while (!done);

			// update data in file
			for (int i = 0; i < this.n; i++) {
				final BmpParser pa = this.shadows.get(i);
				hideByte((byte)evaluations[i].intValue(), j, pa);
			}
			j++;
		} while (consumedBytes < numberOfPixels && (j+1)*k < numberOfPixels);

		writeCreatedShadows();

	}

	/**
	 * Writes files of n shadows that were created
	 */
	private void writeCreatedShadows(){
		for (int i = 0; i < this.n; i++) {
			// write to file
			BmpParser p = this.shadows.get(i);
			BmpWriter writer = new BmpWriter.BmpWriterBuilder(p)
					.file(new File(p.getAbsolutePath()))		//Overwrite shadow header data
					.seed(this.seed)
					.shadowNumber(i+1)
					.secretHeight(secretPicture.getHeight())
					.secretWidth(secretPicture.getWidth())
					.build();
			try {
				writer.writeImage();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Hides one byte of the secret into eight bytes of the shadow
	 * @param byteToHide
	 * @param j
	 * @param shadow
	 */
	private void hideByte(byte byteToHide, int j, BmpParser shadow){
		String bits = Integer.toBinaryString(byteToHide & 255 | 256).substring(1);
		for(int i = 0; i < 8; i++) {
			byte oldByte = shadow.getPictureData()[8*j+i];
			String oldByteStr = Integer.toBinaryString(oldByte & 255 | 256).substring(1);
			String newByteStr = oldByteStr.substring(0,7) + bits.charAt(i);
			byte newByte = (byte)Integer.parseInt(newByteStr, 2);
			shadow.getPictureData()[8*j+i] = newByte;
		}
	}
}
