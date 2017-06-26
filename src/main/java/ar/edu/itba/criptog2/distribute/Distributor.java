package ar.edu.itba.criptog2.distribute;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import ar.edu.itba.criptog2.util.BmpWriter;
import ar.edu.itba.criptog2.util.Polynomial;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.io.IOException;
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
        this.rnd = new Random(seed);						//Random number generator using that seed
        this.shadows = new ArrayList<>();
	}
	
	public static Distributor createFromNamespace(final Namespace ns) throws Exception {
		final Distributor distributor = new Distributor();
		
		distributor.k = ns.getInt("k");
		distributor.n = ns.getInt("n");

		if(distributor.n != -1) {
			if (distributor.n <= 1) {
				System.err.println("N should be greater than or equal to 2.");
				System.err.println("Aborting.");
				System.exit(1);
			}
			if (distributor.k > distributor.n) {
				System.err.println("K should be less than or equal to N.");
				System.err.println("Aborting.");
				System.exit(1);
			}
		}
		if (distributor.k <= 1) {
			System.err.println("K should be greater than or equal to 2.");
			System.err.println("Aborting.");
			System.exit(1);
		}

		try {
			distributor.secretPicture = new BmpParser(ns.getString("secret"));
		} catch (Exception e) {
			System.err.println("Error reading secret picture: " + e.getMessage());
			System.err.println("Aborting.");
			System.exit(1);
		}

		final String shadowDirectory = ns.getString("dir");	// Should never be null
		File[] shadowFiles = new File(shadowDirectory).listFiles();
		if(shadowFiles == null) {
			System.err.println(shadowDirectory + " is not a directory or it does not exist. Aborting.");
			System.exit(1);
		}

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

				if (distributor.k == 8) {
					if (shadow.getWidth() != distributor.secretPicture.getWidth() || shadow.getHeight() != distributor.secretPicture.getHeight()) {
						System.err.println("For K = 8, all shadows should have the same dimensions as secret (" + distributor.secretPicture.getWidth() + "x" + distributor.secretPicture.getHeight() + "), " + shadowFiles[i].getName() + " is " + shadow.getWidth() + "x" + shadow.getHeight());
						System.err.println("Aborting.");
						System.exit(1);
					}
				}

				distributor.shadows.add(shadow);
			}
		}

		int numAvailableShadows = distributor.shadows.size();
		if(numAvailableShadows != distributor.n) {
			if(distributor.n != -1)	{	//Supplied N
				System.err.println("Specified N=" + distributor.n + " but there are " + numAvailableShadows + " available shadows, need exactly " + distributor.n);
				System.err.println("Aborting.");
				System.exit(1);
			} else {
				// Didn't supply N, verify K there are at least K shadows
				if(distributor.k > numAvailableShadows) {
					System.err.println("Specified K=" + distributor.k + " but there are " + numAvailableShadows+ " available shadows, need at least " + distributor.k);
					System.err.println("Aborting.");
					System.exit(1);
				} else {
					// All good, use N = number of available shadows (it is guaranteed K <= N)
					distributor.n = numAvailableShadows;
				}
			}
		}

		return distributor;
	}

	/**
	 * Evaluates the given polynomial in 1, 2, 3, ..., {@link #n} modulo 257.
	 *
	 * @param p The polynomial to evaluate.
	 * @return The evaluations, where the first element is the evaluation in 1.
	 */
	private int[] makeEvaluations(final Polynomial p) {
		int[] evaluations = new int[this.n];
		
		for (int i = 0; i < this.n; i++) {
			evaluations[i] = p.evaluate(i+1) % 257;
		}
		return evaluations;
	}

	/**
	 * Checks whether any of the given values is 256. If so, we should re-evaluate the polynomial that generated these values.
	 *
	 * @param evaluations A polynomial's evaluations.
	 * @return Whether the polynomial that created these values should be re-evaluated.
	 */
	private boolean shouldReevaluate(final int[] evaluations) {
		for (int evaluation : evaluations) {
			if (evaluation == 256) {
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
			int [] evaluations;
			
			do {
			
				evaluations = makeEvaluations(p);
				
				if (shouldReevaluate(evaluations)) {
					for (int i = 0; i < p.getCoefficients().length; i++) {
						// Find the first non-zero coefficient and subtract 1 to it
						int coef = p.getCoefficientAt(i);
						if (coef != 0) {
							p.alterCoefficientAt(i, coef - 1);
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
                hideByte((byte)evaluations[i], j, pa);
			}
			j++;
		} while (consumedBytes < numberOfPixels && (j+1)*k < numberOfPixels);

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
