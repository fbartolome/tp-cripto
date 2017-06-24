package ar.edu.itba.criptog2.distribute;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;

public class Distributor implements Worker {
	
	private Distributor() {
		
	}
	
	public static Distributor createFromNamespace(final Namespace ns) {
		final Distributor distributor = new Distributor();
		
//		Scanner s = new Scanner(System.in);
//		System.out.print("Enter bitmap image path: ");
//		String path = s.nextLine();
        //TODO remove hardcoded string
		String path = "img/Albertssd.bmp";

		BmpParser parser = null;
		try {
			parser = new BmpParser(path);
		} catch (IOException e) {
			System.err.println("Could not read file: " + e.getMessage());
			e.printStackTrace();
		}
		
		// setup distributor
		
		return distributor;
	}
	
	@Override
	public void work() {
		
	}
}
