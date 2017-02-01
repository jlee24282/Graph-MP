
package edu.albany.cs.scoreFuncs;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ToyFunc implements Function {

	private final FuncType funcID;
	private double[] weight;
	private double[][] C;
	private double[][] Cev;
	private double[][] c;
	private double[][] Ct;
	private double[][] ct;

	private double[] tempc;
	EigenDecomposition ed;
	/** vector size */
	private final int n;

	public ToyFunc(double[] weight) {
		this.funcID = FuncType.Unknown;
		this.weight = weight;
		// check input here

		this.n = weight.length;
		this.C = new double[n][n];

		this.Cev = new double[n][n];
		this.c = new double[n][n];
		this.Ct = new double[n][n];
		this.ct = new double[n][n];
		
		for (int i = 0; i < n; i++) {
			Arrays.fill(C[i],0.00D);
			Arrays.fill(c[i], 0.0D);
			Arrays.fill(Ct[i], 0.0D);
			Arrays.fill(ct[i], 0.0D);
			C[i][i] = weight[i];
		}

		RealMatrix CM = new Array2DRowRealMatrix(C);
		for(int i = 0; i<n; i++){
			CM.setRow(i, C[i]);
		}
		ed = new EigenDecomposition(CM);
		for (int i = 0; i < n; i++) {
			Cev[i] = ed.getEigenvector(i).toArray();
		}
		
		//transpose of C
		CM = CM.transpose();
		for(int i = 0; i<n; i++){
			Ct[i] = CM.getRow(i);
		}
		ed = new EigenDecomposition(CM);
		for (int i = 0; i < n; i++) {
			ct[i] = ed.getEigenvector(i).toArray();
		}	
		
		//////
		tempc = new double[n];
		for (int i = 0; i<n; i++){
			tempc[i] = ct[i][i];
		}
		c = C;
	}

	/**
	 * @param x
	 *            get gradient of this function at point x
	 * @return the gradient vector of this function
	 */
		
		@Override
		public double[] getGradient(double[] x) {
			double[] gradient = new double[n];
			
			for (int i = 0; i < n; i++)
				gradient[i] =  (2 * weight[i] * weight[i] * x[i] - weight[i] / 2.0 );
			return gradient;
		}
	/**
	 * @param x
	 *            get value of this function at point x
	 * @return the value of this function
	 */
	@Override
	
	public double getFuncValue(double[] x) {
		double func_value 	= 0.0;
		double L2Norm 		= 0.0;
		double L2Norm2		= 0.0;
		double cx 			= 0.0;
		//c[i]x
		double[] cix = new double[n];
		for (int i = 0; i < n; i++){
			cix[i] = new ArrayRealVector(c[i]).dotProduct(new ArrayRealVector(x));
		}
		L2Norm = new ArrayRealVector(cix).getNorm();
		L2Norm2 = L2Norm*L2Norm;
		
		cx = StatUtils.sum(cix);
		
		//result
		func_value = L2Norm2 - (cx / 2.0);
		
		if (x == null || weight == null || x.length != weight.length) {
			new IllegalArgumentException("Error : Invalid parameters ...");
			System.exit(0);
		}
		
		if (!Double.isFinite(func_value)) {
			System.out.println(funcID + " Error : elevated mean scan stat is not a real value, f is " + func_value);
			System.exit(0);
		}
		return func_value;
	}
	
	
/*
	@Override
	public double[] getGradient(double[] x) {
		double[] gradient = new double[n];
		double[] square_w = square(weight);
		
		double cx = new ArrayRealVector(square_w).dotProduct(new ArrayRealVector(x));
		
		for (int i = 0; i < n; i++)
			//gradient[i] = 2 * weight[i] * weight[i] * x[i] - weight[i] / 2.0 ;
			gradient[i] = 2 * cx - weight[i] / 2.0 ;
		return gradient;
	}

	/**
	 * @param x
	 *            get value of this function at point x
	 * @return the value of this function
	 */
	/*@Override
	public double getFuncValue(double[] x) {
		double func_value = 0.0;

		double[] square_x = square(x);
		double[] square_w = square(weight);

		// System.out.println("\n\n\n print A: " + Arrays.toString(x));
		// System.out.println("print result: " + Arrays.toString(square_x));

		// double sum_square_x = StatUtils.sum(square_x);
		double L2Norm2 = new ArrayRealVector(square_x).dotProduct(new ArrayRealVector(square_w));
		double cx = new ArrayRealVector(x).dotProduct(new ArrayRealVector(weight));
		//double cx = StatUtils.sum(weight);

		if (x == null || weight == null || x.length != weight.length) {
			new IllegalArgumentException("Error : Invalid parameters ...");
			System.exit(0);
		}

		if (L2Norm2 < 0.0D) {
			System.out.println("funcValue error ...");
			System.exit(0);
		} else {
			func_value = L2Norm2 - (cx / 2.0);
			//System.out.println("funcvaoue: " + func_value);
		}
		if (!Double.isFinite(func_value)) {
			System.out.println(funcID + " Error : elevated mean scan stat is not a real value, f is " + func_value);
			System.exit(0);
		}
		return func_value;
	}


*/



	private double[] square(double[] a) {
		double[] result = new double[a.length];

		for (int i = 0; i < n; i++) {
			result[i] = Math.pow(a[i], 2);
		}
		return result;
	}

	@Override
	public double[] getArgMinFx(ArrayList<Integer> S) {
		double[] result = new double[n];

		BigDecimal[] x = argMinFx(this);
		
		for (int i = 0; i < n; i++) {
			if (x[i].doubleValue() < 0 ){
				result[i] = 0.0;
			}else if(x[i].doubleValue() > 1){
				result[i] = 1.0;
			}else{
				result[i] = x[i].doubleValue();
			}
		}

		// fill 0 for constraint
		for(int i = 0 ; i < n ; i++){
			if(!S.contains(i)){
				result[i] = 0.0D;
			}
		}
		
		return result;
	}

	private BigDecimal[] argMinFx(Function func) {
		BigDecimal[] x = new BigDecimal[n];
		
		BigDecimal gamma = new BigDecimal("0.0001");
		BigDecimal err = new BigDecimal(1e-6D); //
		int maximumItersNum = 100;
	
		for (int i = 0; i < x.length; i++) {
			x[i] = new BigDecimal(new Random().nextDouble());
		}
		int iter = 1;
		double smallest = Double.MAX_VALUE;
		BigDecimal[] sx = new BigDecimal[n];

		while (true) {
			
			BigDecimal[] gradient = func.getGradientBigDecimal(x);
			double[] dx = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				dx[i] = x[i].doubleValue();
			}
			BigDecimal oldFuncValue = new BigDecimal(func.getFuncValue(dx));

			for (int i = 0; i < x.length; i++) {
				//Gradient descend
				x[i] = x[i].subtract(gamma.multiply(gradient[i]));
				//Gradient ascend
				//x[i] = x[i].add(gamma.multiply(gradient[i]));
			}
			dx = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				dx[i] = x[i].doubleValue();
			}
			BigDecimal diff = oldFuncValue.subtract(new BigDecimal(func.getFuncValue(dx)));
			
			if(smallest > diff.doubleValue()){
				smallest = diff.doubleValue();
				sx = x;
			}
			if ((diff.compareTo(err) == -1) )
				break;
			if ( iter >= maximumItersNum){
				x = sx;
				break;
			}
			iter++;
		}
		return x;
	}


/**
	 * @return the function ID
	 */
	public FuncType getFuncID() {
		return funcID;
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
	public double getFuncValue(int[] S) {
		double[] x = new double[n];
		Arrays.fill(x, 0.0D);
		for (int i : S) {
			x[i] = 1.0D;
		}
		return getFuncValue(x);
	}
	/**
	 * @param x
	 *            get gradient of this function at point x
	 * @return the gradient vector of this function
	 */
	

	@Override
	public BigDecimal[] getGradientBigDecimal(BigDecimal[] x) {
		double[] xD = new double[n];
		for (int i = 0; i < n; i++) {
			xD[i] = x[i].doubleValue();
		}
		double[] gradient = getGradient(xD);
		BigDecimal[] grad = new BigDecimal[n];
		for (int i = 0; i < n; i++) {
			grad[i] = new BigDecimal(gradient[i]);
		}
		return grad;
	}


	public double[] subtract(double[] a, double b){
		double[] result = new double[n];
		for(int i= 0; i<n; i++){
			result[i] = a[i] - b;
		}
		return result;
	}
	public double[] subtract(double[] a, double[] b){
		double[] result = new double[n];
		for(int i= 0; i<n; i++){
			result[i] = a[i] - b[i];
		}
		return result;
	}
	
	public double[] divide(double[] a, double b){
		double[] result = new double[n];
		for(int i= 0; i<n; i++){
			result[i] = a[i] / b;
		}
		return result;
	}
	public double[] multiply(double[] a, double b){
		double[] result = new double[n];
		for(int i= 0; i<n; i++){
			result[i] = a[i] * b;
		}
		return result;
	}

	

}
