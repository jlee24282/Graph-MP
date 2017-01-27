package edu.albany.cs.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;

import edu.albany.cs.base.ConnectedComponents;
import edu.albany.cs.base.Edge;

public class GeneratePixelDataTest {

    public GeneratePixelDataTest(String[] args) {

    }

    public void generateGridDataWithNoise(int numTrueNodes, String gridDataFileName, double alpha, double noiseLevel,
                                          String outPutFileName, String usedAlgorithm, String dataSource) {

        APDMInputFormat apdm = new APDMInputFormat(gridDataFileName);
        ArrayList<Edge> treEdges = this.randomWalk(apdm.data.graphAdjList, numTrueNodes);
        double[] PValue = new double[apdm.data.numNodes];
        int[] trueNodes = null;
        Arrays.fill(PValue, 1.0);
        for (Edge e : treEdges) {
            PValue[e.i] = alpha;
            PValue[e.j] = alpha;
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
                    PValue[trueNodes[valueToFind]] = 1.0;
                    alreadyDone = ArrayUtils.add(alreadyDone, valueToFind);
                    break;
                }
            }
        }

        int[] normalNodes = ArrayUtils
                .removeElements(new RandomDataGenerator().nextPermutation(PValue.length, PValue.length), trueNodes);
        int noiseLevelInNormalNodes = (int) (((noiseLevel + 0.0) / 100) * (PValue.length - trueNodes.length + 0.0));
        alreadyDone = null;
        for (int j = 0; j < noiseLevelInNormalNodes; j++) {
            while (true) {
                int valueToFind = new Random().nextInt(normalNodes.length);
                if (!ArrayUtils.contains(alreadyDone, valueToFind)) {
                    PValue[normalNodes[valueToFind]] = alpha;
                    alreadyDone = ArrayUtils.add(alreadyDone, valueToFind);
                    break;
                }
            }
        }

        APDMInputFormat.generateAPDMFile(usedAlgorithm, dataSource, apdm.data.newEdges, PValue, null, null, treEdges,
                outPutFileName);
    }

    public void generateGridDataWithNoise(int numTrueNodes, int NumOfNodes, double alpha, double noiseLevel,
                                          String outPutFileName, boolean flag) throws IOException {
        String usedAlgorithm = "Test";
        String dataSource = "GridDataset";
        GenerateSingleGrid g = new GenerateSingleGrid(NumOfNodes);
        ArrayList<Edge> treEdges = randomWalk(g.adj, numTrueNodes);
        double[] weight = new double[g.numOfNodes];
        int[] trueNodes = null;
        Arrays.fill(weight, 1.0);
        Random rand = new Random();
        for (Edge e : treEdges) {
            weight[e.i] = rand.nextDouble() * (1.0D * (new Random().nextInt(20) + 30.0D)) + 5.0D;
            weight[e.j] = rand.nextDouble() * (1.0D * (new Random().nextInt(20) + 30.0D)) + 5.0D;
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
                    weight[trueNodes[valueToFind]] = 1.0;
                    alreadyDone = ArrayUtils.add(alreadyDone, valueToFind);
                    break;
                }
            }
        }

        int[] normalNodes = ArrayUtils
                .removeElements(new RandomDataGenerator().nextPermutation(weight.length, weight.length), trueNodes);

        for (int norm : normalNodes) {
            weight[norm] = rand.nextDouble() * (1.0D * (new Random().nextInt(3) + 2.0D));
        }

        int noiseLevelInNormalNodes = (int) (((noiseLevel + 0.0) / 100) * (weight.length - trueNodes.length + 0.0));
        alreadyDone = null;
        for (int j = 0; j < noiseLevelInNormalNodes; j++) {
            while (true) {
                int valueToFind = new Random().nextInt(normalNodes.length);
                if (!ArrayUtils.contains(alreadyDone, valueToFind)) {
                    weight[normalNodes[valueToFind]] = alpha;
                    alreadyDone = ArrayUtils.add(alreadyDone, valueToFind);
                    break;
                }
            }
        }
        // testBaseCountsEMS(g.edges, treEdges, outPutFileName);
        testBaseCountsEBP(g.edges, treEdges, outPutFileName);
        // APDMInputFormat.generateAPDMFile(usedAlgorithm, dataSource, g.edges,
        // weight, null, null, treEdges, outPutFileName);
    }

    public void testBaseCountsEMS(ArrayList<Edge> edges, ArrayList<Edge> treEdges, String outPutFileName) {
        double[] counts = new double[100];
        double[] base = new double[100];
        HashSet<Integer> nodes = new HashSet<Integer>();
        for (Edge edge : treEdges) {
            nodes.add(edge.i);
            nodes.add(edge.j);
        }
        double[][] data = new double[10][100];
        for (int i = 0; i < 10; i++) {
            double[] randData = new double[100];
            NormalDistribution normAbnormalNodes = new NormalDistribution(5.0D, 1.0);
            NormalDistribution normNormalNodes = new NormalDistribution(5.0D, 1.0);
            for (int j = 0; j < 100; j++) {
                if (nodes.contains(j)) {
                    randData[j] = normAbnormalNodes.sample();
                } else {
                    randData[j] = normNormalNodes.sample();
                }
            }
            data[i] = randData;
        }

        for (int j = 0; j < 100; j++) {
            double totalB = 0.0D;
            for (int k = 0; k < 10; k++) {
                totalB += data[k][j];
            }
            base[j] = totalB / 10.0D;
        }

        NormalDistribution normAbnormalNodes = new NormalDistribution(20.0D, 1.0);
        NormalDistribution normNormalNodes = new NormalDistribution(5.0D, 1.0);
        for (int j = 0; j < 100; j++) {
            if (nodes.contains(j)) {
                counts[j] = normAbnormalNodes.sample();
            } else {
                counts[j] = normNormalNodes.sample();
            }
        }
        APDMInputFormat.generateAPDMFile("Test", "GridData", edges, base, counts, null, treEdges, outPutFileName);
    }

    public void testBaseCountsEBP(ArrayList<Edge> edges, ArrayList<Edge> treEdges, String outPutFileName) {
        double[] counts = new double[100];
        double[] base = new double[100];
        HashSet<Integer> nodes = new HashSet<Integer>();
        for (Edge edge : treEdges) {
            nodes.add(edge.i);
            nodes.add(edge.j);
        }
        double[][] data = new double[10][100];
        for (int i = 0; i < 10; i++) {
            double[] randData = new double[100];
            PoissonDistribution poi = new PoissonDistribution(10.0D);
            for (int j = 0; j < 100; j++) {
                if (nodes.contains(j)) {
                    randData[j] = poi.sample();
                } else {
                    randData[j] = poi.sample();
                }
            }
            data[i] = randData;
        }

        for (int j = 0; j < 100; j++) {
            double totalB = 0.0D;
            for (int k = 0; k < 10; k++) {
                totalB += data[k][j];
            }
            base[j] = totalB / 10.0D;
        }

        PoissonDistribution poiAbnormalNodes = new PoissonDistribution(50.0D);
        PoissonDistribution poiNormalNodes = new PoissonDistribution(10.0D);
        for (int j = 0; j < 100; j++) {
            if (nodes.contains(j)) {
                counts[j] = poiAbnormalNodes.sample();
            } else {
                counts[j] = poiNormalNodes.sample();
            }
        }
        APDMInputFormat.generateAPDMFile("Test", "GridData", edges, base, counts, null, treEdges, outPutFileName);
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
        int graphSize = 100;
        int numOfTrueNodes = 30;
        double alpha = 0.14D;
        double noiseLevel = 0.0D;
        String outputFileName = "data/PixelData/SimulationData/APDM-GridData-" + graphSize + "_noise_" + noiseLevel + "_trueSubSize_"
                + numOfTrueNodes + "_3.txt";
        generateGridDataWithNoise(numOfTrueNodes, graphSize, alpha, noiseLevel, outputFileName, false);
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
                    "data/GridData/APDM-GridData-100_noise_0.0_trueSubSize_30_0.txt");
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
        new GenerateGridData(args).generateSingleCase();
    }
}
