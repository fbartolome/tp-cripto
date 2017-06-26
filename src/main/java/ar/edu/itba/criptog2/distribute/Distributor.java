package ar.edu.itba.criptog2.distribute;

import java.io.File;
import java.io.IOException;
import java.util.*;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import ar.edu.itba.criptog2.util.BmpWriter;
import ar.edu.itba.criptog2.util.Polynomial;
import net.sourceforge.argparse4j.inf.Namespace;

public class Distributor implements Worker {
	
	private int k;
	private int n;
	private BmpParser secretBMPParser;
	private Random rnd;
	private List<BmpParser> carrierBMPParsers;
    private int seed;
	
	private Distributor() {
        this.seed = new Random().nextInt(65536);
        this.rnd = new Random(seed);
        this.carrierBMPParsers = new ArrayList<>();
        try {
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Albertssd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Alfredssd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Audreyssd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Evassd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Facundossd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Gustavossd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Jamesssd.bmp"));
            this.carrierBMPParsers.add(new BmpParser("img/sombrasOriginales/Marilynssd.bmp"));

            this.secretBMPParser = new BmpParser("img/100-75.bmp");
		} catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public static Distributor createFromNamespace(final Namespace ns) throws Exception {
		final Distributor distributor = new Distributor();
		
		distributor.k = ns.getInt("k");
		Optional<Integer> optionalN = Optional.ofNullable(ns.getInt("n"));
		distributor.n = optionalN.orElse(distributor.k);
//
//		if (distributor.k <= 1) {
//			throw new IllegalArgumentException("k should be equal or greater than 2");
//		}
//
//		if (distributor.k > distributor.n) {
//			throw new IllegalArgumentException("k should be equal or smaller than n");
//		}
//
//		distributor.secretBMPParser = new BmpParser(ns.getString("secret"));
//
//		if (distributor.secretBMPParser.getBitsPerPixel() != 8) {
//			throw new IllegalArgumentException("bits per pixel should be 8. instead got " + distributor.secretBMPParser.getBitsPerPixel());
//		}
//
//		final int numberOfPixels = distributor.secretBMPParser.getHeight() * distributor.secretBMPParser.getWidth();
//
//		distributor.randomValues = new ArrayList<>(numberOfPixels);
//
//		distributor.rnd = new Random(distributor.secretBMPParser.getSeed());
//
//		for (int i = 0; i < numberOfPixels; i++) {
//			distributor.randomValues.add(i, distributor.rnd.nextInt(256));
//		}
//
//		final Optional<String> carrierDirectory = Optional.of(ns.getString("dir"));
//
//		final File folder = new File(carrierDirectory.orElse("./"));
//		File[] listOfFiles = folder.listFiles();
//
//		distributor.carrierBMPParsers = new ArrayList<>();
//
//		for (int i = 0; i < listOfFiles.length; i++) {
//			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".bmp")) {
//				final BmpParser carrierParser = new BmpParser(listOfFiles[i].getAbsolutePath());
//				distributor.carrierBMPParsers.add(carrierParser);
//
//				if (carrierParser.getBitsPerPixel() != 8) {
//					throw new IllegalArgumentException(listOfFiles[i].getName() + " should have 8 bits per pixel");
//				}
//
//				if (distributor.k == 8) {
//
//					if (carrierParser.getWidth() != distributor.secretBMPParser.getWidth() || carrierParser.getHeight() != distributor.secretBMPParser.getHeight()) {
//						throw new IllegalArgumentException(listOfFiles[i].getName() + " should be " + distributor.secretBMPParser.getWidth() + "x" + distributor.secretBMPParser.getHeight());
//					}
//				}
//			}
//		}
		
		return distributor;
	}
	
	private int[] makeEvaluations(final Polynomial p) {
		int[] evaluations = new int[this.n];
		
		for (int i = 0; i < this.n; i++) {
			evaluations[i] = p.evaluate(i+1) % 257;
		}
		return evaluations;
	}
	
	private boolean shouldReevaluate(final int[] evaluations) {
		for (int i = 0; i < evaluations.length; i++) {
			if (evaluations[i] == 256) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void work() {
		final int numberOfPixels = this.secretBMPParser.getWidth() * this.secretBMPParser.getHeight();
		final byte[] pictureData = new byte[numberOfPixels];
		
		for (int i = 0; i < numberOfPixels; i++) {
			pictureData[i] = (byte) (this.secretBMPParser.getPictureData()[i] ^ this.rnd.nextInt(256));
		}
		
		int j = 0;
		int consumedBytes = 0;
		
		do {
		
			// build polynomial
			Polynomial p = new Polynomial(0, 0);
			for (int i = 0; i < this.k; i++) {
				p = p.plus(new Polynomial(pictureData[consumedBytes++], i)); //FIXME: pictureData[j + i] ?
			}
			
			boolean done = false;
			int [] evaluations;
			
			do {
			
				evaluations = makeEvaluations(p);
				
				if (shouldReevaluate(evaluations)) {
					for (int i = 0; i < this.k; i++) {
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
				final BmpParser pa = this.carrierBMPParsers.get(i);
                hideByte((byte)evaluations[i],j,pa);
			}
			j++;
		} while (consumedBytes < numberOfPixels && (j+1)*k < numberOfPixels);

        for (int i = 0; i < this.n; i++) {
            // write to file
			BmpParser p = this.carrierBMPParsers.get(i);
            BmpWriter writer = new BmpWriter.BmpWriterBuilder(p)
                    .seed(this.seed).shadowNumber(i+1).file(new File("img/aaa/sombra" + (i+1) + ".bmp"))
                    .secretHeight(secretBMPParser.getHeight()).secretWidth(secretBMPParser.getWidth())
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
        for(int i = 0; i < 8; i++){
			byte oldByte = shadow.getPictureData()[8*j+i];
            String oldByteStr = Integer.toBinaryString(oldByte & 255 | 256).substring(1);
            String newByteStr = oldByteStr.substring(0,7) + bits.charAt(i);
            byte newByte = (byte)Integer.parseInt(newByteStr, 2);
            shadow.getPictureData()[8*j+i] = newByte;
        }
	}
}
