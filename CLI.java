import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLI {
	
	@SuppressWarnings("serial")
	public static class ValidationException extends Exception {
		public ValidationException(String format) {
			super(format);
		} 
	}
	
	public static final String OPT_TRAIN = "train";
	public static final String OPT_PRED = "pred";
	public static final String OPT_EVAL = "eval";
	public static final String OPT_NUM = "n";
	public static final String OPT_DIM = "d";
	public static final String OPT_ORDER = "k";
	
	private static Option trainOption = Option.builder(OPT_TRAIN)
			.hasArg()
			.numberOfArgs(Option.UNLIMITED_VALUES)
			.argName("x.txt y.txt out.model [a | g ss st]")
			.desc("runs the training algorithm over the given input")
			.build();
	private static Option predOption = Option.builder(OPT_PRED)
			.hasArg()
			.numberOfArgs(3)
			.argName("x.txt in.model out.predictions")
			.desc("makes a prediction for an input using a trained model")
			.build();
	private static Option evalOption = Option.builder(OPT_EVAL)
			.hasArg()
			.numberOfArgs(3)
			.argName("x.txt y.txt in.model")
			.desc("evaluates model accuracy using mean squared error")
			.build();
	private static Option numOption = Option.builder(OPT_NUM)
			.required()
			.hasArg()
			.type(Integer.class)
			.desc("number of datapoints in the dataset (number of lines in x.txt)")
			.build();
	private static Option dimOption = Option.builder(OPT_DIM)
			.required()
			.hasArg()
			.type(Integer.class)
			.desc("dimensionality of the input vectors (number of values per line in x.txt)")
			.build();
	private static Option orderOption = Option.builder(OPT_ORDER)
			.required()
			.hasArg()
			.type(Integer.class)
			.desc("order of the polynomial to fit (1 for linear regression)")
			.build();
	private static Option helpOption = Option.builder("h")
			.longOpt("help")
			.hasArg(false)
			.desc("display this help")
			.build();
	
	private static CommandLineParser parser;
	private static Options options;
	private static OptionGroup modeOptions;
	static {
		parser = new DefaultParser();
		
		options = new Options();
		options.addOption(numOption);
		options.addOption(dimOption);
		options.addOption(orderOption);
		options.addOption(helpOption);
		
		modeOptions = new OptionGroup();
		modeOptions.setRequired(true);
		modeOptions.addOption(trainOption);
		modeOptions.addOption(predOption);
		modeOptions.addOption(evalOption);
		options.addOptionGroup(modeOptions);
	}
	
	
	
	public static Settings parseArgs(String[] args) throws ParseException, ValidationException {
		checkForHelp(args);
		
		CommandLine cmd = parser.parse(options, args);
		validateCmd(cmd);
		return new Settings(cmd);
	}
	
	public static void printUsage() {
		HelpFormatter helpFmt = new HelpFormatter();
		helpFmt.printHelp("java Prog1.java", options, true);
	}
	
	
	
	private static void checkForHelp(String[] args) {
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));	
		if (argsList.contains("-h") || argsList.contains("-help")) {
			printUsage();
			System.exit(0);
		}
	}
	
	private static boolean fileExists(String fileName) {
		File f = new File(fileName);
		return f.exists() && f.isFile();
	}
	
	private static boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private static void validateCmd(CommandLine cmd) throws ValidationException, ParseException {
		List<String> modeArgs;
		if (cmd.hasOption(trainOption.getOpt())) {
			modeArgs = Arrays.asList(cmd.getOptionValues(trainOption.getOpt()));
			validateTrainArgs(modeArgs);
		} else if (cmd.hasOption(predOption.getOpt())) {
			modeArgs = Arrays.asList(cmd.getOptionValues(predOption.getOpt()));
			validatePredArgs(modeArgs);
		} else if (cmd.hasOption(evalOption.getOpt())) {
			modeArgs = Arrays.asList(cmd.getOptionValues(evalOption.getOpt()));
			validateEvalArgs(modeArgs);
		}
		
		int d = Integer.parseInt(cmd.getOptionValue(dimOption.getOpt()));
		int k = Integer.parseInt(cmd.getOptionValue(orderOption.getOpt()));
		
		
		
		if (k > 1 && d != 1)
			throw new ValidationException("Error: Input dimension must be 1 when polynomial order > 1");
	}
	
	private static void validateTrainArgs(List<String> modeArgs) throws ValidationException, ParseException {
		if (modeArgs.size() < 4 || (modeArgs.get(3).equals("g") && modeArgs.size() < 6))
			throw new ParseException(String.format("Missing argument for option: %s", trainOption.getOpt()));
		
		for (int i = 0; i < 2; i++) {
			if (!fileExists(modeArgs.get(i)))
				throw new ValidationException(String.format("Error: File %s does not exist", modeArgs.get(i)));
		}
		
		String algorithmMode = modeArgs.get(3);
		if (!algorithmMode.equals("a") && !algorithmMode.equals("g"))
			throw new ValidationException(String.format("Error: Invalid training algorithm option (must be one of: a, g): %s", algorithmMode));
		if (algorithmMode.equals("g")) {
			String ss = modeArgs.get(4);
			String st = modeArgs.get(5);
			if (!isDouble(ss))
				throw new ValidationException(String.format("Error: Step size must be a floating point number: %s", ss));
			if (!isDouble(st))
				throw new ValidationException(String.format("Error: Stop threshold must be a floating point number: %s", st));
		}
	}
	
	private static void validatePredArgs(List<String> modeArgs) throws ValidationException {
		for (int i = 0; i < 2; i++) {
			if (!fileExists(modeArgs.get(i)))
				throw new ValidationException(String.format("Error: File %s does not exist", modeArgs.get(i)));
		}
	}
	
	private static void validateEvalArgs(List<String> modeArgs) throws ValidationException {
		for (int i = 0; i < 3; i++) {
			if (!fileExists(modeArgs.get(i)))
				throw new ValidationException(String.format("Error: File %s does not exist", modeArgs.get(i)));
		}
	}
	
	
}
