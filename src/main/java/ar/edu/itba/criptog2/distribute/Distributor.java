package ar.edu.itba.criptog2.distribute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import ar.edu.itba.criptog2.util.Polynomial;
import net.sourceforge.argparse4j.inf.Namespace;

public class Distributor implements Worker {
	
	private int k;
	private int n;
	private BmpParser secretBMPParser;
	private static final Random rnd = new Random(10);
	private List<Integer> randomValues;
	private List<BmpParser> carrierBMPParsers;
	
	private Distributor() {
		
	}
	
	public static Distributor createFromNamespace(final Namespace ns) throws Exception {
		final Distributor distributor = new Distributor();
		
		distributor.k = ns.getInt("k");
		Optional<Integer> optionalN = Optional.ofNullable(ns.getInt("n"));
		distributor.n = optionalN.orElse(distributor.k);
		
		if (distributor.k <= 1) {
			throw new IllegalArgumentException("k should be equal or greater than 2");
		}
		
		if (distributor.k > distributor.n) {
			throw new IllegalArgumentException("k should be equal or smaller than n");
		}
		
		distributor.secretBMPParser = new BmpParser(ns.getString("secret"));
		
		if (distributor.secretBMPParser.getBitsPerPixel() != 8) {
			throw new IllegalArgumentException("bits per pixel should be 8. instead got " + distributor.secretBMPParser.getBitsPerPixel());
		}
		
		final int numberOfPixels = distributor.secretBMPParser.getHeight() * distributor.secretBMPParser.getWidth();
		
		distributor.randomValues = new ArrayList<>(numberOfPixels);
		
		for (int i = 0; i < numberOfPixels; i++) {
			distributor.randomValues.add(i, rnd.nextInt(256));
		}
		
		final Optional<String> carrierDirectory = Optional.of(ns.getString("dir"));
		
		final File folder = new File(carrierDirectory.orElse("./"));
		File[] listOfFiles = folder.listFiles();
		
		distributor.carrierBMPParsers = new ArrayList<>();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".bmp")) {
				final BmpParser carrierParser = new BmpParser(listOfFiles[i].getAbsolutePath());
				distributor.carrierBMPParsers.add(carrierParser);
				
				if (carrierParser.getBitsPerPixel() != 8) {
					throw new IllegalArgumentException(listOfFiles[i].getName() + " should have 8 bits per pixel");
				}
				
				if (distributor.k == 8) {
				
					if (carrierParser.getWidth() != distributor.secretBMPParser.getWidth() || carrierParser.getHeight() != distributor.secretBMPParser.getHeight()) {
						throw new IllegalArgumentException(listOfFiles[i].getName() + " should be " + distributor.secretBMPParser.getWidth() + "x" + distributor.secretBMPParser.getHeight());
					}
				}
			}
		}
		
		return distributor;
	}
	
	private int[] makeEvaluations(final Polynomial p) {
		int[] evaluations = new int[this.n];
		
		for (int i = 0; i < this.n; i++) {
			evaluations[i] = p.evaluate(i);
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
		
//		final BmpWriterBuilder bmpBuilder = new BmpWriterBuilder();
		final int numberOfPixels = this.secretBMPParser.getWidth() * this.secretBMPParser.getHeight();
		final byte[] fileData = new byte[numberOfPixels];
		
		for (int i = 0; i < numberOfPixels; i++) {
			fileData[i] = (byte) (this.secretBMPParser.getPictureData()[i] ^ this.randomValues.get(i));
		}
		
		int j = 0;
		int consumedBytes = 0;
		
		do {
		
			// build polynomial
			Polynomial p = new Polynomial(0, 0);
			for (int i = 0; i < this.k; i++) {
				p = p.plus(new Polynomial(fileData[consumedBytes], i)); //FIXME: fileData[j + i] ?
				consumedBytes++;
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
				final byte[] picData = pa.getPictureData(); 
				picData[j] = (byte)evaluations[i];
			}
			j++;
		} while (j < numberOfPixels && consumedBytes < numberOfPixels);
		
		for (BmpParser p : this.carrierBMPParsers) {
			// write to file
		}
		
	}
}
