package edu.albany.cs.scoreFuncs;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.StatUtils;

public class G_toy implements Function{
	
	private double[] PValues;
	private int n;
	private final FuncType funcID;
	private double[][] c;
	public G_toy(double[] pValue){
		this.PValues = pValue;
		funcID = FuncType.Unknown;
		
		if(!checkInput(pValue)){
			System.out.println(funcID +  "input parameter is invalid.");
		}
		n = PValues.length;
		c = new double[n][n];
		//Filling up C
		for(int k = 0; k < n; k++){
			Arrays.fill(c[k],0.0D);
			c[k][k] = PValues[k];
		}
	}
	//checking for correct input
	public boolean checkInput(double[] pValue){
		if(pValue == null){
			return false;
		}
		for(double p: pValue){
			if(p<=0.0D)
				return false;
		}
		return true;
	}
	@Override
	public double[] getGradient(double[] x) {
		if(x.length != PValues.length || x == null || PValues == null){
			System.out.println(funcID + "Error: Incorrect input");
		}
		
		double[] Gradient = new double[n];
		for(int i = 0; i < x.length; i++){
			Gradient[i] = -(2*(Math.pow(PValues[i],2)*x[i]) - PValues[i]*0.5);
		}
		//System.out.println(Arrays.toString(Gradient));
		return Gradient;
	}
	
	@Override
	public BigDecimal[] getGradientBigDecimal(BigDecimal[] x) {
		//Getting gradient in BigDecimal Format
		BigDecimal[] grad = new BigDecimal[n];
		double[] x1 = new double[x.length];
		for(int i = 0; i < x.length; i++){
			x1[i] = x[i].doubleValue();
		}
		double[] result = getGradient(x1);
		for(int i = 0; i < x1.length; i++){
			grad[i] = new BigDecimal(result[i]);
		}
		return grad;
	}

//	@Override
//	public BigDecimal[] getGradientBigDecimal(BigDecimal[] x) {
//		double[] xD = new double[n];
//		for (int i = 0; i < n; i++) {
//			xD[i] = x[i].doubleValue();
//		}
//		double[] gradient = getGradient(xD);
//		BigDecimal[] grad = new BigDecimal[n];
//		for (int i = 0; i < n; i++) {
//			grad[i] = new BigDecimal(gradient[i]);
//		}
//		return grad;
//	}


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
		// TODO Auto-generated method stub
		double FuncValue = 0.0D;
		if(x.length != PValues.length || x == null || PValues == null){
			System.out.println(funcID + "Error: Incorrect input");
		}
		double[] sigmaCX = new double[x.length];
		//Multiplying each instance of c with x
		for(int i = 0;i < n; i++){
			sigmaCX[i] = new ArrayRealVector(this.c[i]).dotProduct(new ArrayRealVector(x));
		}
		//Getting L2 Norm
		double result = (new ArrayRealVector(sigmaCX)).getNorm();
		
		FuncValue = StatUtils.sum(sigmaCX);
		FuncValue = Math.pow(result,2) - 0.5*FuncValue;
		//System.out.println(FuncValue+ "\n-------------------------");
		if(!Double.isFinite(FuncValue)){
			System.out.println(funcID + " Error : elevated mean scan stat is not a real value, f is " + FuncValue);
		}
		return -FuncValue;
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
		// TODO Auto-generated method stub
		BigDecimal[] x = argMinFx(this);
		double[] result = new double[x.length];
		//Constraint Check and Projection
		for(int i = 0; i < x.length; i++){
			if(x[i].doubleValue() < 0)
				result[i] = 0.0D;
			else if(x[i].doubleValue() > 1)
				result[i] = 1.0D;
			else
				result[i] = x[i].doubleValue();
		}
		for(int i = 0; i < n; i++){
			if(!S.contains(i)){
				result[i] = 0.0D;
			}
		}
		return result;
	}

	@Override
	public FuncType getFuncID() {
		// TODO Auto-generated method stub
		return this.funcID;
	}
	
	private BigDecimal[] argMinFx(Function func) {
        /** numGraphNodes : defines number of nodes in graph*/
		BigDecimal[] x = new BigDecimal[n];
		/** the step size */
		BigDecimal gamma = new BigDecimal("0.001");
		BigDecimal err = new BigDecimal(1e-6D); //
		int maximumItersNum = 100;
		/** initialize x */
		for (int i = 0; i < x.length; i++) {
			x[i] = new BigDecimal(new Random().nextDouble());
		}
		int iter = 0;
		while (true) {
	    /** get gradient for current iteration*/
			BigDecimal[] gradient = func.getGradientBigDecimal(x);
			double[] dx = new double[x.length];
			
			for (int i = 0; i < x.length; i++) {
				dx[i] = x[i].doubleValue();
			}
			BigDecimal oldFuncValue = new BigDecimal(func.getFuncValue(dx));
		
			for (int i = 0; i < x.length; i++) {
				x[i] = x[i].subtract(gamma.multiply(gradient[i]));
			}
			dx = new double[x.length];
			
			for (int i = 0; i < x.length; i++) {
				dx[i] = x[i].doubleValue();
			}
			BigDecimal diff = oldFuncValue.subtract(new BigDecimal(func.getFuncValue(dx)));
			/** if it is less than error bound or it has more than 100 iterations, it terminates.*/
		
			if ((diff.compareTo(err) == -1) || iter >= maximumItersNum) {
				break;
			}
			iter++;
		}
		return x;
		}


	
	}
