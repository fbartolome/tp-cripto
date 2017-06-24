package ar.edu.itba.criptog2.recover;

import ar.edu.itba.criptog2.Worker;
import net.sourceforge.argparse4j.inf.Namespace;

public class Recoverer implements Worker {

	private Recoverer() {
		
	}
	
	public static Recoverer createFromNamespace(final Namespace ns) {
		final Recoverer recoverer = new Recoverer();
		
		// setup recoverer
		
		return recoverer;
	}
	
	@Override
	public void work() {
		
	}
}
