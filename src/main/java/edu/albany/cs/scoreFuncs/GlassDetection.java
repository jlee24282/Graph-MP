package edu.albany.cs.scoreFuncs;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*
    Simplified Regression
 */
public class GlassDetection implements Function {

    private final double[][] greyValues;
    private final FuncType funcID;
    private final int n;
    private int picIndex;
    private double[][] greyValuesT;
    private final int picCount;
    private int verboseLevel = 1;
    private double[][] c;

    public GlassDetection(double[][] greyValues, int picIndex) {
        this.greyValues     = greyValues;
        this.funcID         = FuncType.Unknown;
        this.n              = greyValues.length;
        this.picIndex       = picIndex;
        this.picCount        = greyValues[0].length;
        this.greyValuesT    = new double[greyValues[0].length][n];

        this.c = new double[n][n];
        double[] temp = new double[n];
        for(int i = 0; i< n; i++){
            temp[i] = greyValues[i][0];
        }

        //transpose
        for(int i = 0; i < n; i++){
            for (int j = 0; j < picCount; j++){
                greyValues[i][j] = greyValues[i][j];///n;///Math.pow(10.0, 2);
                greyValuesT[j][i] = greyValues[i][j];
            }
        }

        for (int i = 0; i < n; i++) {
            c[i][i] = greyValuesT[picIndex][i];
        }
    }


    @Override
    public double[] getGradient(double[] x) {
        if (x == null || greyValues[picIndex] == null ) {
            new IllegalArgumentException("Error : Invalid parameters ...");
            System.exit(0);
        }
//        int s = 0;
//        for (double i : x){
//            if (i > 0)
//                s++;
//        }
        double x0w = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[picIndex]));
        double[] part2 = new double[n];
        Arrays.fill(part2, 0.0);
        for (int k = 0; k < picCount; k++){
            if(k != picIndex){
                double xw = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[k]));
                part2 = addition(part2, multiply(greyValuesT[k], 2*(xw - n)));
            }
        }
        double[] part1 = multiply(greyValuesT[picIndex],(x0w + n)*2);
        double[] gradient = addition(part1, part2);
        return gradient;
    }


    @Override
    public double getFuncValue(double[] x) {
        if (x == null || greyValues[picIndex] == null  ) {
            new IllegalArgumentException("Error : Invalid parameters ...");
            System.exit(0);
        }

        double x0w = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[picIndex]));
        double xkw = 0;
        double sumXW_Z_pow = 0;

//        int s = 0;
//        for (double i : x){
//            if (i > 0)
//                s++;
//        }

        for (int k = 0; k < picCount; k++){
            if(k != picIndex){
                xkw = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[k]));
                sumXW_Z_pow += Math.pow(xkw + n,2);
            }
        }

        double funcScore = (Math.pow((x0w - n), 2) + sumXW_Z_pow);
        return funcScore;
    }


    /**
     * To do maximization
     */
    public double[] getArgMaxFx(ArrayList<Integer> S) {

        double[] result = new double[n];
        double[] x = argMinFx(this);

        for (int i = 0; i < n; i++) {
            if (x[i] < 0) {
                result[i] = 0.0;
            } else if (x[i] > 1) {
                result[i] = 1.0;
            }
            else {
                result[i] = x[i];
            }

        }

        // fill 0 for constraint
        for (int i = 0; i < n; i++) {
            if (!S.contains(i)) {
                result[i] = 0.0D;
            }
        }

        return result;
    }


    @Override
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

    @Override
    public double[] getArgMinFx(ArrayList<Integer> S) {
        // TODO Auto-generated method stub
        double[] x = argMinFx(this);
        double[] result = new double[x.length];
        //Constraint Check and Projection
        for(int i = 0; i < x.length; i++){
            if(x[i] < 0)
                result[i] = 0.0D;
            else if(x[i] > 1)
                result[i] = 1.0D;
            else
                result[i] = x[i];
//            if(x[i].doubleValue() <= 0)
//                result[i] = 0.0D;
//            else
//                result[i] = 1.0D;
        }

        for(int i = 0; i < n; i++){
            if(!S.contains(i)){
                result[i] = 0.0D;
            }
        }

        return result;
    }



    private double[] argMinFx(Function func) {
        /** numGraphNodes : defines number of nodes in graph*/
        double[] x     = new double[n];
        //double gamma    = 0.0001;
        double gamma    = 0.000000007;
        //double gamma    = 0.000000001;
        double err      = 1e-6D; //
        int maximumItersNum = 1000000;


        /** initialize x */
        for (int i = 0; i < x.length; i++) {
            x[i] = new Random().nextDouble();
        }

        int iter = 0;
        while (true) {
            /** get gradient for current iteration*/
            double[] gradient = func.getGradient(x);
            double oldFuncValue = func.getFuncValue(x);
            for (int i = 0; i < x.length; i++) {
                x[i] = x[i] - (gamma*(gradient[i]));
                //x[i] = x[i].add(gamma.multiply(gradient[i]));
            }
            double currentFunc = func.getFuncValue(x);
            double diff = Math.abs(oldFuncValue - currentFunc);
            /** if it is less than error bound or it has more than 100 iterations, it terminates.*/

            if ( diff < err ) {
                System.out.println("CONVERGE: " + iter);
                break;
            }

            if(iter>= maximumItersNum){
                //System.out.println("NUMBER");
                break;
            }
            if(iter %10000 == 0) {
                System.out.println(ArrayUtils.toString(gradient));
                System.out.println(diff);
                if( Double.isNaN(diff)){
                    System.out.println("NAN" + oldFuncValue + " "+ currentFunc );
                }
            }
            iter++;
        }
        return x;
    }





    //getter picIndex
    public int getPicIndex(){
        return this.picIndex;
    }


    private double[] multiply(double[] a, double b){
        double[] result = new double[n];
        for(int i= 0; i<n; i++){
            result[i] = a[i] * b;
        }
        return result;
    }


    private double[] addition(double[] a, double[] b){
        double[] result = new double[n];
        for(int i= 0; i < n; i++){
            result[i] = a[i] + b[i];
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
}