import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Util {
	
	public static RealMatrix readMatrixFile(File file, int n, int d) {
		RealMatrix mat = null;
		
		try (Stream<String> stream = Files.lines(file.toPath())) {
			Pattern p = Pattern.compile(" ");
			double[][] arr = stream.map(line -> p.splitAsStream(line)
								   				 .mapToDouble(Double::parseDouble)
								   				 .toArray())
								   .toArray(double[][]::new);
			
			mat = MatrixUtils.createRealMatrix(arr);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(3);
		}
		
		assert mat.getRowDimension() == n : String.format("%s: read %d rows, expected %d", file, mat.getRowDimension(), n);
		assert mat.getColumnDimension() == d : String.format("%s: read %d cols, expected %d", file, mat.getColumnDimension(), d);;
		
		return mat;
	}
	
	public static void writeMatrixFile(File file, RealMatrix mat) {
		double[][] data = mat.getData();
		
		Stream<String> strData = Arrays.stream(data).map(Util::joinDoubles);
		try {
			Files.write(file.toPath(), (Iterable<String>)strData::iterator, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(3);
		}
		
		System.out.println(String.format("File %s written successfully.", file));
	}
	
	private static String joinDoubles(double[] arr) {
		StringBuilder sb = new StringBuilder();
		int len = arr.length;
		for (int i = 0; i < len - 1; i++) {
			sb.append(String.format("%.3e ", arr[i]));
		}
		sb.append(String.format("%.3e", arr[len-1]));
		return sb.toString();
	}
	
	public static RealMatrix prependOnesColumn(RealMatrix mat) {
		RealMatrix newMat;
		
		int n = mat.getRowDimension();
		int d = mat.getColumnDimension();
		
		newMat = mat.createMatrix(n, d+1);
		newMat.setSubMatrix(Util.ones(n).getData(), 0, 0);
		newMat.setSubMatrix(mat.getData(), 0, 1);
		
		return newMat;
	}
	
	public static RealMatrix ones(int n) {
		return constants(1.0, n);
	}
	
	public static RealMatrix zeros(int n) {
		return constants(0.0, n);
	}
	
	public static RealMatrix constants(double val, int n) {
		double[][] data = new double[n][1];
		for (int i = 0; i < n; i++)
			data[i] = new double[]{ val };
		return MatrixUtils.createRealMatrix(data);
	}
	
	public static double mse(RealMatrix H, RealMatrix Y) {
		RealMatrix R;
		
		// Obtain the residuals by subtracting Y from H
		R = H.subtract(Y);
		
		// Sum the squared residuals to obtain MSE
		return R.walkInColumnOrder(new DefaultRealMatrixPreservingVisitor() {

			double mse = 0.0;
			
			@Override
			public void visit(int row, int column, double value) {
				this.mse += value*value;
			}
			@Override
			public double end() {
				return this.mse;
			}
		});
	}
	
	public static RealMatrix elementWiseMultiply(RealMatrix A, RealMatrix B) {
		RealMatrix C = A.copy();
		C.walkInColumnOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int col, double val) {
				return val * B.getEntry(row, col);
			}
		});
		return C;
	}
	
	public static RealMatrix polynomialExpansion(RealMatrix X, int k) {
		RealMatrix X_poly = X.createMatrix(X.getRowDimension(), k);
		
		RealMatrix base = X.getColumnMatrix(0);
		RealMatrix prev = base;
		
		X_poly.setColumnMatrix(0, base);
		
		int i = 0;
		while (i < k-1) {
			X_poly.setColumnMatrix(++i, elementWiseMultiply(prev, base));
			prev = X_poly.getColumnMatrix(i);
		}
		
		return X_poly;
	}
}
