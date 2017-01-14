package edu.albany.cs.scoreFuncs;
import edu.albany.cs.base.ArrayIndexSort;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GaussianLLR implements Function {

    private final double[][] greyValues;
    private final FuncType funcID;
    private final int n;
    private double[] b;
    private double[][] c;
    private int picIndex;
    private double q;

    private int verboseLevel = 1;

    public GaussianLLR(double[][] greyValues) {
        this.greyValues = greyValues;
        this.picIndex = 0;
        this.funcID = FuncType.Unknown;
        this.n = greyValues.length;
        this.b = new double[n];
        this.c = new double[12][n];
        double mean, std;
        this.picIndex = 0;
        for(int i = 0; i< n; i++){
            mean = getMedian(greyValues[i]);
            std = getMAD(greyValues[i]);
            b[i] = mean*mean/(std*std);
            for (int pic = 0; pic< 12; pic ++){
                c[pic][i] = mean*greyValues[i][pic]/(std*std);
            }
        }
    }

    private double getMean(double[] values){
        double result = 0.0;
        double sum = 0.0;

        for (int i = 0; i < values.length; i ++){
            sum += values[i];
        }
        result = sum/values.length;
        return result;
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
            std = std + Math.pow(values[i] - medianValue, 2);
        }
        std = std/values.length;
        std = Math.sqrt(std);
        return std;
    }

    private double getStd(double[] values){
        double mean = getMean(values);
        double std = 0.0;

        //System.out.println(ArrayUtils.toString(values));
        for (int i=0; i<values.length;i++) {
            std = std + Math.pow(values[i] - mean, 2);
        }
        std = std/values.length;
        std = Math.sqrt(std);
        return std;
    }
    @Override
    public double[] getGradient(double[] x) {
        if (x == null || c == null ) {
            new IllegalArgumentException("Error : Invalid parameters ...");
            System.exit(0);
        }
        double[] gradient = new double[n];
        double B = new ArrayRealVector(x).dotProduct(new ArrayRealVector(b));
        picIndex = calcPicIndex(x);
        double C = new ArrayRealVector(x).dotProduct(new ArrayRealVector(c[picIndex]));

        if (B == 0.0D) {
            System.out.println(funcID + " Error : the denominator should not be zero.");
            System.exit(0);
        }
        for (int i = 0; i < gradient.length; i++) {
            gradient[i] =-( (C/B-1)*c[picIndex][i] + (1- Math.pow(C/B,2)/2) * b[i]);
        }
        return gradient;
    }
    private int calcPicIndex(double[] x){
        int picIn = 0;

        double minLLR = Double.MAX_VALUE;
        int picin = -1;
        //get picIndex
        for(int i = 0; i< 12; i ++){
            double B = new ArrayRealVector(x).dotProduct(new ArrayRealVector(b));
            double C = new ArrayRealVector(x).dotProduct(new ArrayRealVector(c[i]));
            if(C/B > 1 && StatUtils.sum(x) != 0){
                if(minLLR > getLLR(x, b, c[i])){
                    minLLR = getLLR(x, b, c[i]);
                    picin = i;
                }
                //System.out.print(" INDEX " + i + " " + getLLR(x, b, c[i]));
                for(int j = 0; j< n; j++){
                    if(x[j] != 0) {
                        //System.out.print((greyValues[j][i] + " "));
                    }
                }
                //System.out.println();
            }
        }
        //System.out.println("picIn" + picin);
        return picIn;
    }
    @Override
    public double getFuncValue(double[] x) {
        if (x == null || c == null ) {
            new IllegalArgumentException("Error : Invalid parameters ...");
            System.exit(0);
        }
        picIndex = calcPicIndex(x);
        double llrScore = getLLR(x, b, c[picIndex]);
        if (!Double.isFinite(llrScore)) {
            System.out.println(funcID + " Error : elevated mean scan stat is not a real value, f is " + llrScore);
            System.exit(0);
        }

        return -llrScore;
    }
    private double getLLR(double[] x, double[] b, double[] c){
        double B = new ArrayRealVector(x).dotProduct(new ArrayRealVector(b));
        double C = new ArrayRealVector(x).dotProduct(new ArrayRealVector(c));
        double llrScore = Math.pow((C-B),2)/(2*B);

        return llrScore;
    }
    /**
     * calculate function xlog(x/a)
     */
    private double calXlogXA(double x, double a) {
        if (x <= 0.0D) {
            return 0.0D;
        } else {
            if (a <= 0.0D) {
                System.out.println("function xlog(x/a) is error");
                System.exit(0);
            }
            return x * Math.log(x / a);
        }
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
        BigDecimal gamma = new BigDecimal("0.0001");
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
                //x[i] = x[i].subtract(gamma.multiply(gradient[i]));
                x[i] = x[i].add(gamma.multiply(gradient[i]));
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
