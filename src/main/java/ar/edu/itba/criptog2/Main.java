package ar.edu.itba.criptog2;

import ar.edu.itba.criptog2.distribute.Distributor;
import ar.edu.itba.criptog2.recover.Recoverer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Main class, program runner.
 */
public class Main {

	public static void main(String[] args) throws Exception {

		// https://argparse4j.github.io/usage.html
		final ArgumentParser parser = ArgumentParsers.newArgumentParser("visualSSS").defaultHelp(true).description("Place a description here.");

		//Required Arguments
		parser.addArgument("-d").dest("distribute").action(Arguments.storeTrue()).required(false).type(String.class).help("Action is to distribute image");
		parser.addArgument("-r").dest("recover").action(Arguments.storeTrue()).required(false).type(String.class).help("Action is to recover image");
		parser.addArgument("-secret").dest("secret").required(true).type(String.class).help("File to distribute or recover");
		parser.addArgument("-k").dest("k").required(true).type(Integer.class).help("k to use");

		//Optional Arguments
		parser.addArgument("-n").dest("n").required(false).type(Integer.class).help("n to use");
		parser.addArgument("-dir").dest("dir").required(false).type(String.class).setDefault("./").help("Directory");
		
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		//Exception when one chooses to recover and to distribute an image
		if(ns.getBoolean("distribute") && ns.getBoolean("recover")){
			throw new IllegalArgumentException("You can only choose to recover or to distribute an image, not both");
		}


		if (ns.getBoolean("distribute")) {
			Distributor.createFromNamespace(ns).work();
		} else if(ns.getBoolean("recover")){
			Recoverer.createFromNamespace(ns).work();
		}else{
			throw new IllegalArgumentException("Choose if you want to distribute (-d) or recover an image (-r)");
		}
	}
}
