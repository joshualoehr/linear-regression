import org.apache.commons.math3.linear.RealMatrix;

public class GradientDescentOptimizer implements Optimizer {

	RealMatrix W_hat, X_hat, Y;
	
	private double loss = Double.MAX_VALUE;
	private double relativeReduction = Double.MAX_VALUE;
	
	private final double stepSize;
	private final double stopThreshold;
	
	public GradientDescentOptimizer(RealMatrix X, RealMatrix Y, double stepSize, double stopThreshold) {
		X_hat = Util.prependOnesColumn(X);
		this.Y = Y;
		
		W_hat = Util.zeros(X_hat.getColumnDimension());
		
		this.stepSize = stepSize;
		this.stopThreshold = stopThreshold;
	}
	
	@Override
	public RealMatrix train() {
		while (relativeReduction > stopThreshold) {
			W_hat = W_hat.subtract(gradients().scalarMultiply(stepSize));
			computeRelativeReduction();
		}
		return W_hat;
	}
	
	private void computeRelativeReduction() {
		double newLoss = Util.mse(outputs(), Y);
		relativeReduction = (loss - newLoss) / loss;
		loss = newLoss;
	}
	
	private RealMatrix outputs() {
		return X_hat.multiply(W_hat);
	}
	
	private RealMatrix residuals() {
		RealMatrix H = outputs();
		return H.subtract(Y);
	}
	
	private RealMatrix gradients() {
		RealMatrix G, R;
		
		int n = X_hat.getRowDimension();
		
		R = residuals();
		G = X_hat.transpose().multiply(R).scalarMultiply(2.0 / n);
		
		return G;
	}
}
