import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;

public class Settings {
	
	private static final String ALG_A = "a";
	private static final String ALG_G = "g";
	
	private String mode;
	private File inputFile;
	private File targetFile;
	private File modelFile;
	private File predFile;
	
	// Training mode settings
	private String alg;
	private double ss;
	private double st;
	
	private int n, d, k;
	
	
	public Settings(CommandLine cmd) {
		if (cmd.hasOption(CLI.OPT_TRAIN)) {
			mode = CLI.OPT_TRAIN;
		} else if (cmd.hasOption(CLI.OPT_PRED)) {
			mode = CLI.OPT_PRED;
		} else {
			mode = CLI.OPT_EVAL;
		}
		
		List<String> modeArgs;
		if (cmd.hasOption(CLI.OPT_TRAIN)) {
			modeArgs = Arrays.asList(cmd.getOptionValues(CLI.OPT_TRAIN));
			
			inputFile = new File(modeArgs.get(0));
			targetFile = new File(modeArgs.get(1));
			modelFile = new File(modeArgs.get(2));
			alg = modeArgs.get(3);
			
			if (alg.equals(ALG_G)) {
				ss = Double.parseDouble(modeArgs.get(4));
				st = Double.parseDouble(modeArgs.get(5));
			}
			
		} else if (cmd.hasOption(CLI.OPT_PRED)) {
			modeArgs = Arrays.asList(cmd.getOptionValues(CLI.OPT_PRED));
			
			inputFile = new File(modeArgs.get(0));
			modelFile = new File(modeArgs.get(1));
			predFile = new File(modeArgs.get(2));
			
		} else {
			modeArgs = Arrays.asList(cmd.getOptionValues(CLI.OPT_EVAL));
			
			inputFile = new File(modeArgs.get(0));
			targetFile = new File(modeArgs.get(1));
			modelFile = new File(modeArgs.get(2));
		}
		
		n = Integer.parseInt(cmd.getOptionValue(CLI.OPT_NUM));
		d = Integer.parseInt(cmd.getOptionValue(CLI.OPT_DIM));
		k = Integer.parseInt(cmd.getOptionValue(CLI.OPT_ORDER));
	}
	
	public boolean isAnalytic() {
		return alg.equals(ALG_A);
	}
	
	public boolean isGradientDescent() {
		return alg.equals(ALG_G);
	}
	
	public boolean doTrain() {
		return mode.equals(CLI.OPT_TRAIN);
	}
	
	public boolean doPred() {
		return mode.equals(CLI.OPT_PRED);
	}
	
	public boolean doEval() {
		return mode.equals(CLI.OPT_EVAL);
	}
	
	public File getInputFile() {
		return inputFile;
	}
	
	public File getTargetFile() {
		return targetFile;
	}
	
	public File getModelFile() {
		return modelFile;
	}
	
	public File getPredFile() {
		return predFile;
	}
	
	public double getStepSize() {
		return ss;
	}
	
	public double getStopThreshold() {
		return st;
	}
	
	public int getNum() {
		return n;
	}
	
	public int getDim() {
		return d;
	}
	
	public int getOrder() {
		return k;
	}
}
