
package edu.albany.cs.scoreFuncs;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.StatUtils;
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
    private double mean;
    private double std;     //standard deviation
    private double q;
    private double[] means; //median for each i
    private double[] stds;  //MAD for each i
    private double[] medians; //median for each i
    private double[] MADs;  //MAD for each i


    /** vector size */
    public GlassDetection(double[][] greyValue) {
        this.greyValues        = greyValue;
        n                        = greyValue.length;
        funcID                  = FuncType.Unknown;
        means                   = new double[n];
        stds                    = new double[n];
        medians                 = new double[n];
        MADs                    = new double[n];

        c = new double[n][n];

        for(int i = 0; i< n; i++){
            medians[i]  = getMedian(greyValues[i]); //median
            MADs[i]     = getMAD(greyValues[i]);     //MAD
            means[i]    = getMean(greyValues[i]);
            stds[i]     = getStd(greyValues[i]);
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
        double[] g1 = new double[n];
        double[] g2 = new double[n];
        double B = 0, C = 0;

        C = new ArrayRealVector(divide(means, pow(stds))).dotProduct(new ArrayRealVector(x));
        B = StatUtils.sum(divide(pow(means),pow(stds)));

        for(int i = 0; i< n; i++){
            gradient[i] = -((C/B - 1) * means[i] / (Math.pow(stds[i], 2)));
        }

        return gradient;
    }

    /**
     * @param x
     *            get value of this function at point x
     * @return the value of this function
     */
    @Override

    public double getFuncValue(double[] x) {
        double llrScore, llrScore1, llrScore2;
        double B = 0.0, C = 0.0, q = 0.0;

        C = new ArrayRealVector(divide(means, pow(stds))).dotProduct(new ArrayRealVector(x));
        B = StatUtils.sum(divide(pow(means),pow(stds)));
        llrScore = Math.pow((C-B),2)/2*B;

        this.q = C/B;
        return -llrScore;
    }
    public double getQ(){
        return this.q;
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

        BigDecimal gamma = new BigDecimal("0.00001");
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
                //x[i] = x[i].subtract(gamma.multiply(gradient[i]));
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

        for (int i = 0; i < values.length; i ++){
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
        std = std/values.length;
        std = Math.sqrt(std);
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

    public double[] divide(double[] a, double[] b) {
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = a[i] / b[i];
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
    public double[] pow(double[] a) {
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            result[i] = Math.pow(a[i],2) ;
        }
        return result;
    }

}
