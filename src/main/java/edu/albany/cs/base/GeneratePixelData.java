package edu.albany.cs.base;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

import java.io.IOException;
import java.util.*;

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
        N = 11;
        c = 100;
        numOfTrueNodes = 30;
        graphSize = M*N;
    }

    public void generateGridDataWithNoise(int numTrueNodes, int NumOfNodes, double noiseLevel,
                                          String outPutFileName, boolean flag) throws IOException {

        GenerateMNSingleGraph g = new GenerateMNSingleGraph(M, N);
        ArrayList<Edge> treEdges = randomWalk(g.adj, numTrueNodes);
        double[] weight = new double[g.numOfNodes];
        int[] trueNodes = null;
        Arrays.fill(weight, 1.0);
        Random rand = new Random();
        for (Edge e : treEdges) {
            weight[e.i] = rand.nextInt(256) + this.c;
            weight[e.j] = rand.nextInt(256) + this.c;

            if(weight[e.i] < 0)
                weight[e.i] = 0;
            else if(weight[e.i] >255)
                weight[e.i] = 255;
            if(weight[e.j] < 0)
                weight[e.j] = 0;
            else if(weight[e.j] >255)
                weight[e.j] = 255;

            if (!ArrayUtils.contains(trueNodes, e.i)) {
                trueNodes = ArrayUtils.add(trueNodes, e.i);
            }
            if (!ArrayUtils.contains(trueNodes, e.j)) {
                trueNodes = ArrayUtils.add(trueNodes, e.j);
            }
        }

        int noiseLevelInTrueNodes = (int) (((noiseLevel + 0.0) / 100) * (trueNodes.length));
        int[] alreadyDone = null;
        for (int k = 0; k < noiseLevelInTrueNodes; k++) {
            while (true) {
                int valueToFind = new Random().nextInt(trueNodes.length);
                if (!ArrayUtils.contains(alreadyDone, valueToFind)) {
                    weight[trueNodes[valueToFind]] = 255.0;
                    alreadyDone = ArrayUtils.add(alreadyDone, valueToFind);
                    break;
                }
            }
        }

        int[] normalNodes = ArrayUtils
                .removeElements(new RandomDataGenerator().nextPermutation(weight.length, weight.length), trueNodes);

        for (int norm : normalNodes) {
            weight[norm] = rand.nextInt(256);
        }

        int noiseLevelInNormalNodes = (int) (((noiseLevel + 0.0) / 100) * (weight.length - trueNodes.length + 0.0));
        alreadyDone = null;
        for (int j = 0; j < noiseLevelInNormalNodes; j++) {
            while (true) {
                int valueToFind = new Random().nextInt(normalNodes.length);
                if (!ArrayUtils.contains(alreadyDone, valueToFind)) {
                    weight[normalNodes[valueToFind]] = 255;
                    alreadyDone = ArrayUtils.add(alreadyDone, valueToFind);
                    break;
                }
            }
        }

        genData(g.edges, treEdges, outPutFileName);
    }

    private HashMap<int[], Double> aList2HMap(ArrayList<Edge>  trueNodes){
        HashMap<int[], Double> tEdge = new HashMap<int[], Double>();

        for(int i = 0; i< trueNodes.size(); i++){
            int[] key = new int[]{trueNodes.get(i).i, trueNodes.get(i).j};
            tEdge.put(key, 1.0);
        }

        return tEdge;
    }

    public void genData(ArrayList<Edge> edges, ArrayList<Edge> treEdges, String outPutFileName){
        double[][] weight = new double[graphSize][12];
        double[] count = new double[graphSize];
        meanNorm = 120;
        meanAbNorm = meanNorm - this.c;
        stdNorm = 1;
        stdAbNorm = 1;


        HashMap<int[], Double> trueSubGraphEdges 	= aList2HMap(treEdges);

        for( int i = 0; i<graphSize; i++){
            count[i] = 0.0;
        }

        HashSet<Integer> nodes = new HashSet<Integer>();
        for (Edge edge : treEdges) {
            nodes.add(edge.i);
            nodes.add(edge.j);
        }


        NormalDistribution normAbnormalNodes = new NormalDistribution(meanAbNorm, stdAbNorm);
        NormalDistribution normNormalNodes = new NormalDistribution(meanNorm, stdNorm);
        for (int j = 0; j < graphSize; j++) {
            if (nodes.contains(j)) {
                weight[j][0] = (int) normAbnormalNodes.sample();
            } else {
                weight[j][0] = (int) normNormalNodes.sample();
            }
            if(weight[j][0] < 0)
                weight[j][0] = 0;
            else if(weight[j][0] >255)
                weight[j][0] = 255;
        }


        APDMInputFormat.generateAPDMFilePixel("Test", "RandomData", edges, weight,
                count,  trueSubGraphEdges,  outPutFileName);
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
        while (h.size() < numTrueNodes) {
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

    public void generateSingleCase() throws IOException {
        double noiseLevel = 0.0D;

        String outputFileName = "data/PixelData/APDM-" +M+"X"+N+ "_C" + this.c + "_trueSubSize_"
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
        new GeneratePixelData(args).generateSingleCase();
        System.out.println("DONE");
    }
}
