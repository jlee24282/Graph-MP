package edu.albany.cs.scoreFuncs;

import edu.albany.cs.base.ArrayIndexSort;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ProtestScanStat implements Function {

    private final double[] Lambda;
    private final int[][] X;
    private final double[] Z;
    private final HashSet<Integer> R;
    private final int size;
    private final FuncType funcType = FuncType.ProtestScanStat;
    private double q;

    /**
     * @param Lambda = [lambda1,lambda2,...,lambdan]
     * @param X = X[|R|,n]
     * @param R = {0,1,2,...,|R|-1}
     */
    public ProtestScanStat(double[] Lambda, int[][] X, HashSet<Integer> R) {
        this.Lambda = Lambda;
        this.X = X;
        double[] z = new double[X.length];
        this.R = R;
        for (int i = 0; i < X.length; i++) {
            double sum = 0.0D;
            for (int j : R) {
                sum += X[i][j];
            }
            z[i] = sum;
        }
        Z = z;
        size = Lambda.length;
    }

    /** get gradient at point y
     * @see edu.albany.cs.scoreFuncs.Function#getGradient(double[])
     */
    @Override
    public double[] getGradient(double[] y) {

        double[] gradient = new double[size];
        double cardinalityR = R.size();
        double ZY = new ArrayRealVector(y).dotProduct(new ArrayRealVector(Z));
        double LambdaY = new ArrayRealVector(y).dotProduct(new ArrayRealVector(Lambda));
        /** calculate q*/
        q = ZY / (cardinalityR * LambdaY);
        if (q >= 1.0D) {
            System.out.println("gradient warning : q is >= 1.0D");
            System.out.println(q);
        }
        for (int i = 0; i < size; i++) {
            double item1 = (cardinalityR - ZY / LambdaY) * Lambda[i];
            double item2 = (Math.log(ZY) - Math.log(LambdaY) - Math.log(cardinalityR)) * Z[i];
            gradient[i] = item1 + item2;
            if (!Double.isFinite(gradient[i])) {
                System.out.println("gradient error ...");
                System.exit(0);
            }
        }

        return gradient;
    }

    /** gradient at y
     * @see edu.albany.cs.scoreFuncs.Function#getGradientBigDecimal(java.math.BigDecimal[])
     */
    @Override
    public BigDecimal[] getGradientBigDecimal(BigDecimal[] y) {
        double cardinalityR = R.size();
        BigDecimal[] result = new BigDecimal[size];
        double ZY = 0.0D;
        double LambdaY = 0.0D;
        for (int i = 0; i < size; i++) {
            ZY += y[i].multiply(new BigDecimal(Z[i])).doubleValue();
            LambdaY += y[i].multiply(new BigDecimal(Lambda[i])).doubleValue();
        }
        /** calculate q*/
        q = ZY / (cardinalityR * LambdaY);
        if (q >= 1.0D) {
            System.out.println("gradient warning : q is >= 1.0D");
            System.out.println(q);
        }
        for (int i = 0; i < size; i++) {
            double item1 = (cardinalityR - ZY / LambdaY) * Lambda[i];
            double item2 = (Math.log(ZY) - Math.log(LambdaY) - Math.log(cardinalityR)) * Z[i];
            result[i] = new BigDecimal(item1 + item2);
        }
        return result;
    }

    @Override
    public double[] getGradient(int[] S) {
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            if (ArrayUtils.contains(S, i)) {
                y[i] = 1.0D;
            } else {
                y[i] = 0.0D;
            }
        }
        return getGradient(y);
    }
    
    @Override
    public double getFuncValue(double[] y) {
        double cardinalityR = R.size();
        double ZY = 0.0D;
        double LambdaY = 0.0D;
        for(int i = 0 ; i < y.length ; i++){
        	ZY += y[i]*Z[i];
        	LambdaY += y[i]*Lambda[i];
        }
        /** calculate q*/
        q = ZY / (cardinalityR * LambdaY);
        if (q >= 1.0D) {
            System.out.println("warning : q is "+q+">= 1.0D");
        }
        double item1 = cardinalityR * LambdaY - ZY;
        double item2 = ZY * (Math.log(ZY) - Math.log(LambdaY) - Math.log(cardinalityR));
        /** funcValue = item1 + item2*/
        return item1 + item2;
    }

    /**
     * @param y the input vector y
     * @return q, q is the result of analytic value
     */
    public double getQ(double[] y){
        double cardinalityR = R.size();
        double ZY = new ArrayRealVector(y).dotProduct(new ArrayRealVector(Z));
        double LambdaY = new ArrayRealVector(y).dotProduct(new ArrayRealVector(Lambda));
        return ZY / (cardinalityR * LambdaY);
    }

    @Override
    public double getFuncValue(int[] S) {
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            if (ArrayUtils.contains(S, i)) {
                y[i] = 1.0D;
            } else {
                y[i] = 0.0D;
            }
        }
        return getFuncValue(y);
    }

    @Override
    public double[] getArgMinFx(ArrayList<Integer> S) {
        Double[] priority = new Double[S.size()];
        for (int i = 0; i < priority.length; i++) {
            double priorityFuncValue = 1.0D / Z[S.get(i)];// ranking
            if (Z[S.get(i)] == 0.0D) {
                priorityFuncValue = Double.MAX_VALUE;
            }
            priority[i] = priorityFuncValue;
        }
        ArrayIndexSort arrayIndexComparator = new ArrayIndexSort(priority);
        Integer[] indexes = arrayIndexComparator.getIndices();
        /** sort by decreasing manner*/
        Arrays.sort(indexes, arrayIndexComparator);
        /**v_1,v_2,...,v_m*/
        ArrayList<Integer> sortedS = new ArrayList<>();
        for (int index : indexes) {
            sortedS.add(S.get(index));
        }
        double maxF = -Double.MAX_VALUE;
        double[] y = new double[size];
        List<Integer> tmpRk = new ArrayList<>();
        for (int k = 1; k <= sortedS.size(); k++) {
            List<Integer> Rk = sortedS.subList(0, k);
            Arrays.fill(y, 0.0D);
            for (int index : Rk) {
                y[index] = 1.0D;
            }
            double fk = getFuncValue(y);
            if ((fk > maxF) && (q < 0.9) && (Double.isFinite(fk))) {
                maxF = fk;
                tmpRk = new ArrayList<>(Rk);
            }
        }
        Arrays.fill(y, 0.0D);
        for(int k:tmpRk){
            y[k] = 1.0D;
        }
        return y;
    }

    public double[] getX0(){
        double[] x0 = new double[size];
        double minVal = Double.MAX_VALUE;
        for (int i = 0; i < x0.length; i++) {
            double sumX = 0.0D;
            for (int j = 0; j < R.size(); j++) {
                sumX += X[i][j];
            }
            if (sumX < minVal) {
                minVal = sumX;
            }
            if (sumX / (R.size() * 1.0D) < Lambda[i]) {
                x0[i] = 1.0D;
            } else {
                x0[i] = 0.0D;
            }
        }
        return x0;
    }

    @Override
    public FuncType getFuncID() {
        return funcType;
    }

    /**
     *  @return vector Z where Z[i] = \sum_{t \in R}{X_i^t}
     */
    public double[] getZ() {
        return Z;
    }
    
    public double getQ(){
    	return q;
    }

}
