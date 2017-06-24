package ar.edu.itba.criptog2.distribute;

import java.io.IOException;
import java.util.Scanner;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import net.sourceforge.argparse4j.inf.Namespace;

public class Distributor implements Worker {
	
	private Distributor() {
		
	}
	
	public static Distributor createFromNamespace(final Namespace ns) {
		final Distributor distributor = new Distributor();
		
		Scanner s = new Scanner(System.in);
		System.out.print("Enter bitmap image path: ");
		String path = s.nextLine();
		BmpParser parser = new BmpParser(path);
		try {
			byte[][] data = parser.readAllFiles();
			System.out.println(data);
		} catch (IOException e) {
			System.err.println("Could not read all files: " + e.getMessage());
			e.printStackTrace();
		}
		
		// setup distributor
		
		return distributor;
	}
	
	@Override
	public void work() {
		
	}
}
