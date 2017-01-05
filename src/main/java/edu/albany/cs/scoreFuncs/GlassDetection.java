
package edu.albany.cs.scoreFuncs;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GlassDetection implements Function {

    private final FuncType funcID;
    private double[][] greyValues;
    private double[][] c;
    private final int n;    //size of node
    private double mean;    //median
    private double std;     //MAD
    private double[] means; //median for each i
    private double[] stds;  //MAD for each i


    /** vector size */
    public GlassDetection(double[][] greyValue) {
        this.greyValues = greyValue;
        funcID = FuncType.Unknown;

        n = greyValue.length;
        c = new double[n][n];

        for(int i = 0; i< n; i++){
            means[i]    = getMedian(greyValues[i]); //median
            stds[i]     = getMAD(greyValues[i]);     //MAD
        }
    }


    /**
     * @param x
     *            get gradient of this function at point x
     * @return the gradient vector of this function
     */
    @Override
    public double[] getGradient(double[] x) {
        double[] gradient = new double[n];
        double B = 0, C = 0;

        return gradient;
    }

    /**
     * @param x
     *            get value of this function at point x
     * @return the value of this function
     */
    @Override

    public double getFuncValue(double[] x) {
        double llrScore;
        double B = 0, C = 0;

        for(int i = 0; i< n ; i++){
            mean   = getMean(this.greyValues[i]); //median
            std    = getStd(this.greyValues[i]);     //MAD

            B += Math.pow(mean, 2)/std;

            if(x[i] == 1.0){
                C += mean/std;
                NormalDistribution ndNodes = new NormalDistribution(means[i], stds[i]);
                NormalDistribution ndNodes2 = new NormalDistribution(mean, std);
            }
        }


        llrScore = Math.pow((C-B),2)/2*B;
        return llrScore;
    }

    @Override
    public double[] getArgMinFx(ArrayList<Integer> S) {
        double[] result = new double[n];

        BigDecimal[] x = argMaxFx(this);

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

    /**
     * To do maximization
     */
    public double[] getArgMaxFx(ArrayList<Integer> S) {

        double[] result = new double[n];

        BigDecimal[] x = argMaxFx(this);

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

    private BigDecimal[] argMaxFx(Function func) {
        BigDecimal[] x = new BigDecimal[n];

        BigDecimal gamma = new BigDecimal("0.0001");
        BigDecimal err = new BigDecimal(1e-10D); //
        int maximumItersNum = 500;

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
                x[i] = x[i].add(gamma.multiply(gradient[i]));
            }
            dx = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                dx[i] = x[i].doubleValue();
            }
            BigDecimal diff = oldFuncValue.subtract(new BigDecimal(func.getFuncValue(dx)));
            // System.out.print(diff.doubleValue());
            if (smallest > diff.abs().doubleValue()) {
                smallest = diff.abs().doubleValue();
                sx = x;
            }
            if ((diff.compareTo(err) == -1))
                break;
            if (iter >= maximumItersNum) {
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

    private double getMean(double[] values){
        double mean = 0.0;
        double sum = 0.0;

        for (int i = 0; i < n; i ++){
            sum += values[i];
        }
        mean = sum/n;
        return mean;
    }

    private double getStd(double[] values){
        double mean = getMean(values);
        double std = 0.0;

        for (int i=0; i<values.length;i++) {
            std = std + Math.pow(values[i] - mean, 2);
        }
        return std;
    }

    private double getMedian(double[] values){
        Median median = new Median();
        double medianValue = median.evaluate(values);
        return medianValue;
    }

    private double getMAD(double[] values){
        Median median = new Median();
        double medianValue = median.evaluate(values);
        double std = 0.0;

        for (int i=0; i<values.length;i++) {
            std = std + Math.pow(values[i] - mean, 2);
        }
        return std;
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

    public double[] subtract(double[] a, double b) {
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] - b;
        }
        return result;
    }

    public double[] subtract(double[] a, double[] b) {
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    public double[] divide(double[] a, double b) {
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] / b;
        }
        return result;
    }

    public double[] multiply(double[] a, double b) {
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] * b;
        }
        return result;
    }

}
