package edu.albany.cs.scoreFuncs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a simple example to show how to use our GraphMP algorithm.
 * 
 * f(x) = ||x||_2^{2} - \frac{1}{2} x^{T}1
 * 
 * @author baojian
 *
 */
public class RegularizedScore implements Function {

	private final int n;
	private final FuncType funcType;

	public RegularizedScore(int n) {
		this.n = n;
		this.funcType = FuncType.RegularizedScore;
	}

	@Override
	public double[] getGradient(double[] x) {
		if (x == null || x.length == 0) {
			return null;
		}
		if (x.length != n) {
			System.out.println("x.length should be equal to n");
			System.exit(0);
		}
		/** get gradient of our score function */
		double[] gradient = new double[x.length];
		for (int i = 0; i < n; i++) {
			gradient[i] = 2 * x[i] - 0.5D;
		}
		return gradient;
	}

	@Override
	public BigDecimal[] getGradientBigDecimal(BigDecimal[] x) {
		if (x == null || x.length == 0) {
			return null;
		}
		double[] input = null;
		System.arraycopy(x, 0, input, 0, x.length);
		BigDecimal[] result = null;
		System.arraycopy(getGradient(input), 0, result, 0, x.length);
		return result;
	}

	@Override
	public double[] getGradient(int[] S) {
		double[] x = new double[n];
		Arrays.fill(x, 0.0D);
		for (int i : S) {
			x[i] = 1.0D;
		}
		return getGradient(x);
	}

	@Override
	public double getFuncValue(double[] x) {
		if (x == null || x.length == 0) {
			System.out.println("input parameter error.");
			System.exit(0);
		}
		double funcVal = 0.0D;
		for (int i = 0; i < x.length; i++) {
			funcVal += x[i] * x[i] - 0.5D * x[i];
		}
		return funcVal;
	}

	@Override
	public double getFuncValue(int[] S) {
		double[] x = new double[n];
		Arrays.fill(x, 0.0D);
		for (int i : S) {
			x[i] = 1.0D;
		}
		return getFuncValue(x);
	}

	@Override
	public double[] getArgMinFx(ArrayList<Integer> S) {

		return null;
	}

	@Override
	public FuncType getFuncID() {
		return this.funcType;
	}

}