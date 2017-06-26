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
		final ArgumentParser parser = ArgumentParsers.newArgumentParser("VisualSSS")
                .description("Distribute secret pictures among other pictures, and recover secret pictures from other pictures.")
                .usage("-d|-r -secret [FILE] -k [NUMBER] [-n [NUMBER] -dir [DIRECTORY]]");

        //Required Arguments
        //Note: -d and -r are both set as required: false because exactly one of the two is required
		parser.addArgument("-d").dest("distribute").action(Arguments.storeTrue()).required(false).type(String.class).help("Distribute an image");
		parser.addArgument("-r").dest("recover").action(Arguments.storeTrue()).required(false).type(String.class).help("Recover an image");
		parser.addArgument("-secret").dest("secret").required(true).type(String.class).help("If used with -d, the image to distribute. If used with -r, the output file for the recovered image.");
		parser.addArgument("-k").dest("k").required(true).type(Integer.class).help("The minimum number of shadows needed to recover an image.");

		//Optional Arguments
		parser.addArgument("-n").dest("n").required(false).type(Integer.class).help("Only allowed when used with -d. Total number of shadows to generate. If not provided, will make n the number of pictures in the specified directory.");
		parser.addArgument("-dir").dest("dir").required(false).type(String.class).setDefault(".").help("If used with -d, directory containing images where secret will be distributed. If used with -r, directory containing images from which to recover the secret. In either case, default is current directory.");

		Namespace ns = null;
		try {
            ns = parser.parseArgs(args);
            //Extra validations
            if(ns.getBoolean("distribute").equals(ns.getBoolean("recover"))) {
                throw new ArgumentParserException("Exactly ONE of -d (distribute) or -r (recover) is needed.", parser);
            }
            if(ns.getInt("n") != null && ns.getBoolean("recover")) {
				throw new ArgumentParserException("-n is only allowed with -d", parser);
			}
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }


		if (ns.getBoolean("distribute")) {
			Distributor.createFromNamespace(ns).work();
		} else if(ns.getBoolean("recover")){
			Recoverer.createFromNamespace(ns).work();
		}
	}
}
