import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;

public class AnalyticOptimizer implements Optimizer {
	
	RealMatrix X_hat, W_hat, Y;

	public AnalyticOptimizer(RealMatrix X, RealMatrix Y) {
		X_hat = Util.prependOnesColumn(X);
		this.Y = Y;
		
		W_hat = Util.zeros(X_hat.getColumnDimension() + 1);
	}
	
	@Override
	public RealMatrix train() {
		try {
			// Analytically solve for W: W* = (X_hat^T * X_hat)^-1 * X_hat^T * Y
			W_hat = MatrixUtils.inverse(X_hat.transpose().multiply(X_hat)).multiply(X_hat.transpose()).multiply(Y);
			
		} catch (SingularMatrixException e) {
			System.out.println("Error: X^T*X is singular, analytical solution not possible.");
			System.exit(4);
		}
		return W_hat;
	}

}
