package ar.edu.itba.criptog2;

import ar.edu.itba.criptog2.distribute.Distributor;
import ar.edu.itba.criptog2.recover.Recoverer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Main class, program runner.
 */
public class Main {

	public static void main(String[] args) {

		// https://argparse4j.github.io/usage.html

		final ArgumentParser parser = ArgumentParsers.newArgumentParser("visualSSS").defaultHelp(true).description("Place a description here.");

		parser.addArgument("-a", "--action").dest("action").required(true).type(String.class).choices("distribute", "recover").help("Specify action");
		parser.addArgument("-s", "--secret").dest("secret").required(true).type(String.class).help("File to distribute or recover");
		parser.addArgument("-k").dest("k").required(true).type(Integer.class).help("k to use");
		parser.addArgument("-n").dest("n").required(false).type(Integer.class).help("n to use");
		parser.addArgument("-d", "--dir").dest("dir").required(false).type(String.class).setDefault("./").help("Directory");
		
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}

		if (ns.getString("action").equals("distribute")) {
			Distributor.createFromNamespace(ns).work();
		} else {
			Recoverer.createFromNamespace(ns).work();
		}
	}
}
