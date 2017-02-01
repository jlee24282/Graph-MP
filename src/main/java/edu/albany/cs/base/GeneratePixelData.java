package edu.albany.cs.base;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class GeneratePixelData {

    private double c;
    private double meanAbNorm;
    private double meanNorm;
    private double stdNorm;
    private double stdAbNorm;
    private int N;
    private int M;
    private int numOfTrueNodes;
    int graphSize;

    public GeneratePixelData(String[] args) {
        M = 10;
        N = 10;
        c = 1000;
        numOfTrueNodes = 5;
        graphSize = M*N;
    }

    public void generateGridDataWithNoise(int numTrueNodes, int NumOfNodes, double noiseLevel,
                                          String outPutFileName, boolean flag) throws IOException {

        GenerateMNSingleGraph g = new GenerateMNSingleGraph(M,N);
       // ArrayList<Edge> treEdges = randomWalk(g.adj, numTrueNodes);
        ArrayList<Edge> treEdges = bestRandomWalk(g.adj, numTrueNodes);
        int[] trueNodes = null;
        for (Edge e : treEdges) {
            if (!ArrayUtils.contains(trueNodes, e.i)) {
                trueNodes = ArrayUtils.add(trueNodes, e.i);
            }
            if (!ArrayUtils.contains(trueNodes, e.j)) {
                trueNodes = ArrayUtils.add(trueNodes, e.j);
            }
        }

        genData(g.edges, treEdges, outPutFileName);
    }

    private HashMap<int[], Double> aList2HMap(ArrayList<Edge>  trueNodes){
        HashMap<int[], Double> tEdge = new HashMap<int[], Double>();

        for(int i = 0; i< trueNodes.size(); i++){
            int[] key = new int[]{trueNodes.get(i).i, trueNodes.get(i).j};
            //System.out.println("Test" + ArrayUtils.toString(key));
            tEdge.put(key, 1.0);
        }

        return tEdge;
    }

    public void genData(ArrayList<Edge> edges, ArrayList<Edge> treEdges, String outPutFileName){
        double[][] weight = new double[graphSize][12];
        double[] count = new double[graphSize];
        meanNorm = 70;
        meanAbNorm = meanNorm - this.c;
        stdNorm = 1;
        stdAbNorm = 1;

        HashMap<int[], Double> trueSubGraphEdges 	= aList2HMap(treEdges);

        for( int i = 0; i<graphSize; i++){
            count[i] = 0.0;
            for(int j = 0; j<12; j++)
                weight[i][j] = 0.0;
        }

        HashSet<Integer> nodes = new HashSet<Integer>();
        for (Edge edge : treEdges) {
            nodes.add(edge.i);
            nodes.add(edge.j);
        }

//        NormalDistribution normAbnormalNodes = new NormalDistribution(10+c, stdAbNorm);
//        NormalDistribution normNormalNodes = new NormalDistribution(10, stdNorm);

        PoissonDistribution normAbnormalNodes = new PoissonDistribution(10.0);
        PoissonDistribution normNormalNodes = new PoissonDistribution(10+c);

        for (int j = 0; j < graphSize; j++) {
            //abnormal picture -> weight[j][0]
            if (nodes.contains(j)) {
                weight[j][0] = 1.5*(int) normAbnormalNodes.sample();
            } else {
                weight[j][0] = (int) normNormalNodes.sample();
            }
            //normal pictures
            for(int k = 1; k < 12; k++){
                weight[j][k] = (int) normNormalNodes.sample();
            }
        }

        APDMInputFormat.generateAPDMFilePixel("Test", "PixelData", edges, weight,
                count,  trueSubGraphEdges,  outPutFileName);
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

    private ArrayList<Edge> bestRandomWalk(ArrayList<ArrayList<Integer>> arr, int numTrueNodes) {
        ArrayList<Edge> bestTreEdges = null;
        double bestESTD = Double.MAX_VALUE;

        for(int i = 0; i < 1000; i++) {
            ArrayList<Edge> treEdges = randomWalk(arr, numTrueNodes);
            double[] treEdgeArr = new double[treEdges.size()+1];
            int idx = 0;
            for (Edge e : treEdges) {
                treEdgeArr[idx++] = e.i;
                treEdgeArr[idx] = e.j;
            }
            double std = getStd(treEdgeArr);
            if(std < bestESTD && treEdges.size() == numTrueNodes-1){
                bestESTD = std;
                bestTreEdges = treEdges;
            }
        }
        return bestTreEdges;
    }

    /**
     * get number of nodes using the random walk algorithm
     *
     * @param arr
     *            the graph adjacency list
     * @param numTrueNodes
     *            the number of true nodes in the random walk algorithm
     * @return the edges generated by the random walk algorithm
     */
    private ArrayList<Edge> randomWalk(ArrayList<ArrayList<Integer>> arr, int numTrueNodes) {

        ArrayList<Edge> trueSubGraph = new ArrayList<Edge>();
        Random random = new Random();
        int start = random.nextInt(arr.size());
        HashSet<Integer> h = new HashSet<Integer>();
        h.add(start);
        int count = 0;
        int iteration = 0;
        while (h.size() < numTrueNodes) {
            iteration++;
            if(iteration == 1000)
                break;
            int next = arr.get(start).get(random.nextInt(arr.get(start).size()));
            if (h.contains(next)) {
                continue;
            }
            h.add(next);
            trueSubGraph.add(new Edge(start, next, count++, 1.00000));
            start = next;
        }
        ArrayList<Edge> reducedTreEdges = new ArrayList<Edge>();
        for (Edge edge : trueSubGraph) {
            if (!checkExist(reducedTreEdges, edge)) {
                reducedTreEdges.add(edge);
            }
        }
        return reducedTreEdges;
    }

    /**
     * check the edge exists in the trueSubGraph
     *
     * @param trueSubGraph
     * @param edge
     * @return true if this edge contains in trueSubGraph else will return false
     */
    private boolean checkExist(ArrayList<Edge> trueSubGraph, Edge edge) {
        if (trueSubGraph.isEmpty()) {
            return false;
        }
        for (Edge ed : trueSubGraph) {
            if ((ed.i == edge.i && ed.j == edge.j) || (ed.i == edge.j && ed.j == edge.i)) {
                return true;
            }
        }
        return false;
    }

    public void generateSingleCase(double c) throws IOException {
        double noiseLevel = 0.0D;
        this.c = c;
        String outputFileName = "data/PixelData/SimulationData/AbLow/APDM-" +M+"X"+N+ "_C" + this.c + "_trueSubSize_"
                + numOfTrueNodes + ".txt";
        generateGridDataWithNoise(numOfTrueNodes, graphSize, noiseLevel, outputFileName, false);
        testTrueSubGraph(outputFileName);
    }

    public void testTrueSubGraph(String fileName) {
        APDMInputFormat apdm = new APDMInputFormat(fileName);
        System.out.println(apdm.data.trueSubGraphNodes.length);
        ConnectedComponents cc = apdm.data.cc;
        System.out.println(cc.computeCCSubGraph(apdm.data.trueSubGraphNodes));
    }

    public static void main(String args[]) throws IOException {
        int verbose = 1;
        if (verbose > 1) {
            APDMInputFormat apdm = new APDMInputFormat(
                    "data/PixelData/APDM-10X11_C40.0_trueSubSize_30.txt");
            System.out.println("apdm.data.connective: " + apdm.data.connective);
            for (int ab : apdm.data.trueSubGraphNodes) {
                System.out.println(ab + " " + apdm.data.PValue[ab]);
            }
            System.out.println("----------------------------");
            for (int ab : apdm.data.V) {
                if (!ArrayUtils.contains(apdm.data.trueSubGraphNodes, ab)) {
                    System.out.println(ab + " " + apdm.data.PValue[ab]);
                }
            }
            Utils.stop();
        }
  //      new GeneratePixelData(args).generateSingleCase(0.0);
//        new GeneratePixelData(args).generateSingleCase(5.0);
//        new GeneratePixelData(args).generateSingleCase(10.0);
//        new GeneratePixelData(args).generateSingleCase(30.0);
//        new GeneratePixelData(args).generateSingleCase(50.0);
//        new GeneratePixelData(args).generateSingleCase(100.0);
//        new GeneratePixelData(args).generateSingleCase(150.0);
          new GeneratePixelData(args).generateSingleCase(100.0);
//        new GeneratePixelData(args).generateSingleCase(300.0);
//        new GeneratePixelData(args).generateSingleCase(500.0);
//        new GeneratePixelData(args).generateSingleCase(1000.0);
//        new GeneratePixelData(args).generateSingleCase(10000.0);
        System.out.println("DONE");
    }
}
