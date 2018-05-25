import java.io.File;

import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.linear.RealMatrix;

public class Prog1 {
	
	public static void main(String args[]) {
		Settings settings = null;
		try {
			settings = CLI.parseArgs(args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			CLI.printUsage();
			System.exit(1);
		} catch (CLI.ValidationException e) {
			System.out.println(e.getMessage());
			System.exit(2);
		}
		
		File inputFile, targetFile, modelFile, predFile;
		
		int n = settings.getNum();
		int d = settings.getDim();
		int k = settings.getOrder();				
		
		double ss, st;
		
		if (settings.doTrain()) {
			inputFile = settings.getInputFile();
			targetFile = settings.getTargetFile();
			modelFile = settings.getModelFile();
			
			if (settings.isAnalytic()) {
				trainAnalytic(inputFile, targetFile, modelFile, n, d, k);
				
			} else if (settings.isGradientDescent()) {
				ss = settings.getStepSize();
				st = settings.getStopThreshold();
				
				trainGradientDescent(inputFile, targetFile, modelFile, ss, st, n, d, k);
			}
			
		} else if (settings.doPred()) {
			inputFile = settings.getInputFile();
			modelFile = settings.getModelFile();
			predFile = settings.getPredFile();
			
			predict(inputFile, modelFile, predFile, n, d, k);
			
		} else if (settings.doEval()) {
			inputFile = settings.getInputFile();
			targetFile = settings.getTargetFile();
			modelFile = settings.getModelFile();
			
			evaluate(inputFile, targetFile, modelFile, n, d, k);
		}
	}
	
	private static void trainAnalytic(File inputFile, File targetFile, File modelFile, int n, int d, int k) {
		RealMatrix X, Y, W;
		Optimizer optimizer;
		
		X = Util.readMatrixFile(inputFile, n, d);
		Y = Util.readMatrixFile(targetFile, n, 1);
		
		if (k > 1)
			X = Util.polynomialExpansion(X, k);
		
		optimizer = new AnalyticOptimizer(X, Y);
		W = optimizer.train();
		
		Util.writeMatrixFile(modelFile, W.transpose());
	}
	
	private static void trainGradientDescent(File inputFile, File targetFile, File modelFile, double ss, double st, int n, int d, int k) {
		RealMatrix X, Y, W;
		Optimizer optimizer;
		
		X = Util.readMatrixFile(inputFile, n, d);
		Y = Util.readMatrixFile(targetFile, n, 1);
		
		if (k > 1)
			X = Util.polynomialExpansion(X, k);
		
		optimizer = new GradientDescentOptimizer(X, Y, ss, st);
		W = optimizer.train();
		
		Util.writeMatrixFile(modelFile, W.transpose());
	}

	private static void predict(File inputFile, File modelFile, File predFile, int n, int d, int k) {
		RealMatrix X, W, Y;
		
		X = Util.readMatrixFile(inputFile, n, d);
		W = Util.readMatrixFile(modelFile, 1, d+1);
		
		if (k > 1)
			X = Util.polynomialExpansion(X, k);
		
		Y = predict(X, W);
		
		Util.writeMatrixFile(predFile, Y);
	}
	
	private static RealMatrix predict(RealMatrix X, RealMatrix W) {
		RealMatrix X_hat, Y;
		
		X_hat = Util.prependOnesColumn(X);
		Y = X_hat.multiply(W.transpose());
		
		return Y;
	}
	
	private static void evaluate(File inputFile, File targetFile, File modelFile, int n, int d, int k) {
		RealMatrix X, W, H, Y;
		double mse;
		
		// Load inputs and model weights
		X = Util.readMatrixFile(inputFile, n, d);
		W = Util.readMatrixFile(modelFile, 1, k > 1 ? k+1 : d+1);
		
		if (k > 1)
			X = Util.polynomialExpansion(X, k);
		
		// Make predictions H from X input and W model
		H = predict(X, W);
		
		// Load the true Y target values
		Y = Util.readMatrixFile(targetFile, n, 1);
		
		mse = Util.mse(H, Y);
		System.out.println(String.format("%.3e", mse));
	}
	
	
}
