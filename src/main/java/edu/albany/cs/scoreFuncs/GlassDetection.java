package edu.albany.cs.scoreFuncs;
import edu.albany.cs.base.ArrayIndexSort;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.StatUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public GlassDetection(double[][] greyValues) {
        this.greyValues     = greyValues;
        this.funcID         = FuncType.Unknown;
        this.n              = greyValues.length;
        this.picIndex       = 0;
        this.picCount        = greyValues[0].length;
        this.greyValuesT    = new double[greyValues[0].length][n];

        this.c = new double[n][n];
        for(int i = 0; i < n; i++){
            for (int j = 0; j < picCount; j++){
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
        x = divide(x,  x.length);
        double x0w = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[picIndex]));
        double[] part2 = new double[n];
        double[] part4 = new double[n];
        Arrays.fill(part2, 0.0);
        Arrays.fill(part4, 0.0);

        for (int k = 0; k < picCount; k++){
            if(k != picIndex){
                double xw = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[k]));
                //part2 = addition(part2, multiply(greyValuesT[k],(200-2)));
                part2 = addition(part2, multiply(greyValuesT[k], 2*(xw-1)));
            }
        }
        for (int k = 0; k < picCount; k++){
            if(k != picIndex){
                double xw = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[k]));
                part4 = addition(part4, multiply(x, 2*(xw-1)));
            }
        }

        double[] part1 = multiply(greyValuesT[picIndex],(x0w + 1)*2);
        //double[] part1 = multiply(greyValuesT[picIndex],(+2));
        double[] part3 = multiply(x, (x0w + 1)*2);

        //double[] gradient = addition(part1, part2);
        double[] gradient = addition(addition(part1, part2), addition(part3, part4));
//        return part1;
        return gradient;
    }


    @Override
    public double getFuncValue(double[] x) {
        if (x == null || greyValues[picIndex] == null  ) {
            new IllegalArgumentException("Error : Invalid parameters ...");
            System.exit(0);
        }
        x = divide(x,  x.length);

        double x0w = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[picIndex]));
        double sumXW_Z_pow = 0;

        for (int k = 0; k < picCount; k++){
            if(k != picIndex){
                double xkw = new ArrayRealVector(x).dotProduct(new ArrayRealVector(greyValuesT[k]));
                sumXW_Z_pow += Math.pow(xkw-1,2);
            }
        }

        double funcScore = Math.pow((x0w+1), 2) + sumXW_Z_pow;
        return funcScore;
    }


    /**
     * To do maximization
     */
    public double[] getArgMaxFx(ArrayList<Integer> S) {

        double[] result = new double[n];

        BigDecimal[] x = argMinFx(this);

        for (int i = 0; i < n; i++) {
            if (x[i].doubleValue() < 0) {
                result[i] = 0.0;
            } else if (x[i].doubleValue() > 1) {
                result[i] = 1.0;
            } else {
                result[i] = x[i].doubleValue();
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



    private BigDecimal[] argMinFx(Function func) {
        /** numGraphNodes : defines number of nodes in graph*/
        BigDecimal[] x = new BigDecimal[n];
        /** the step size */
        BigDecimal gamma = new BigDecimal("0.0000000000005");
        BigDecimal err = new BigDecimal(1e-5D); //
        int maximumItersNum = 500;
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
                //x[i] = x[i].add(gamma.multiply(gradient[i]));
            }
            dx = new double[x.length];

            for (int i = 0; i < x.length; i++) {
                dx[i] = x[i].doubleValue();
            }
            BigDecimal diff = oldFuncValue.subtract(new BigDecimal(func.getFuncValue(dx)));
            diff = diff.abs();
            /** if it is less than error bound or it has more than 100 iterations, it terminates.*/

            if ((diff.compareTo(err) == -1) ) {
                System.out.println("CONVERGE: " + iter);
                System.out.println(ArrayUtils.toString(gradient));

                break;
            }
            if(iter>= maximumItersNum){
                //System.out.println("NUMBER");
                break;
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
