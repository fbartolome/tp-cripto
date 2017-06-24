package ar.edu.itba.criptog2.distribute;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
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
	private BmpParser secretBMPParser;
	private static final Random rnd = new Random(10);
	private List<Integer> randomValues;
	private List<BmpParser> carrierBMPParsers;
	
	private Distributor() {
		
	}
	
	public static Distributor createFromNamespace(final Namespace ns) throws Exception {
		final Distributor distributor = new Distributor();
		
		distributor.k = ns.getInt("k");
		distributor.n = ns.getInt("n");
		
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
			distributor.randomValues.set(i, rnd.nextInt(256));
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
				
				if (carrierParser.getWidth() != distributor.secretBMPParser.getWidth() || carrierParser.getHeight() != distributor.secretBMPParser.getHeight()) {
					throw new IllegalArgumentException(listOfFiles[i].getName() + " should be " + distributor.secretBMPParser.getWidth() + "x" + distributor.secretBMPParser.getHeight());
				}
			}
		}
		
		return distributor;
	}
	
	@Override
	public void work() {
		
	}
}
