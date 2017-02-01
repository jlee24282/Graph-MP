package edu.albany.cs.graphMP;

import edu.albany.cs.base.ConnectedComponents;
import edu.albany.cs.base.Utils;
import edu.albany.cs.headApprox.PCSFHead;
import edu.albany.cs.scoreFuncs.Function;
import edu.albany.cs.tailApprox.PCSFTail;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

/**
 * Algorithm 1 : Graph-Mp Aglorithm in our IJCAI paper.
 *
 * @author Baojian bzhou6@albany.edu
 */
public class GraphMP {

	/** 1Xn dimension, input data */
	private final double[] c;
	private final int graphSize;
	
	/** graph info */
	private final HashSet<Integer> nodes;
	private final ArrayList<Integer[]> edges;
	private final ArrayList<Double> edgeCosts;
	
	/** the total sparsity of S */
	private final int s;
	
	/** the maximum number of connected components formed by F */
	private final int g;
	
	/** bound on the total weight w(F) of edges in the forest F */
	
	private final double B;
	/** number of iterations */
	private final int t;
	private final int[] trueSubGraph;
	private final boolean singleNodeInitial;
	private final Function function;

	/** results */
	public double[] x;
	public int[] resultNodes_supportX;
	public int[] resultNodes_Tail = null;
	public double funcValue = -1.0D;
	public double runTime;

	private int verboseLevel = 10;
	private int check = 1;

	public GraphMP(ArrayList<Integer[]> edges, ArrayList<Double> edgeCosts, double[] c, int s, int g, double B, int t,
			boolean singleNodeInitial, int[] trueSubGraph, Function func, String resultFileName) {
		this(edges, edgeCosts, c, s, g, B, t, singleNodeInitial, trueSubGraph, func, resultFileName, null);
	}

	public GraphMP(ArrayList<Integer[]> edges, ArrayList<Double> edgeCosts, double[] c, int s, int g, double B, int t,
			boolean singleNodeInitial, int[] trueSubGraph, Function func, String resultFileName, String fileName) {
		this.edges = edges;
		this.nodes = new HashSet<>();
		for (Integer[] edge : edges) {
			nodes.add(edge[0]);
			nodes.add(edge[1]);
		}
		this.graphSize = nodes.size();
		this.edgeCosts = edgeCosts;
		this.c = c;
		this.s = s;
		this.g = g;
		this.B = B;
		this.t = t;
		this.singleNodeInitial = singleNodeInitial;
		this.trueSubGraph = trueSubGraph;
		this.function = func;
		x = run(); // run the algorithm
	}

	private double[] run() {

		long startTime = System.nanoTime();
		double[] x;
		if (singleNodeInitial) {
			/** return X0 with only one entry has 1.0D value */
			x = this.initializeX_RandomSingleNode();
		} else {
			/** return X0 with maximum connected component nodes with 1.0D */
			x = initializeX_Random();
		}
		ArrayList<Double> fValues = new ArrayList<>();
		for (int i = 0; i < this.t; i++) { // t iterations
			if (verboseLevel > 0) {
				System.out.println("============================iteration: " + i + "============================");
			}
			fValues.add(function.getFuncValue(x));
			double[] gradientF = function.getGradient(x);
			
			if(check == 0){
				System.out.println(fValues);
				System.out.println("Gradient F: " + Arrays.toString(gradientF));
				System.out.println("x: " + Arrays.toString(x));
			}
			
			gradientF = normalizeGradient(x, gradientF);
			if(check == 0){
				System.out.println("Normalized F: " + Arrays.toString(gradientF));
				check++;
			}
			
			/** head approximation */
			PCSFHead pcsfHead = new PCSFHead(edges, edgeCosts, gradientF, s, g, B, trueSubGraph);
			ArrayList<Integer> S = unionSets(pcsfHead.bestForest.nodesInF, support(x));
			
			/** tail approximation */
			//System.out.print("\n\n getargminfx called/ Print S: ");
			//System.out.println(S);
			double[] b = function.getArgMinFx(S);
			//System.out.println("Print b: " + Arrays.toString(b));
			PCSFTail pcsfTail = new PCSFTail(edges, edgeCosts, b, s, g, B, trueSubGraph);
			
			/** calculate x^{i+1} */
			for (int j = 0; j < b.length; j++) {
				x[j] = 0.0D;
			}
			int test = 0;
			for (int j : pcsfTail.bestForest.nodesInF) {
				x[j] = b[j];
//				System.out.println(x[j]);
//				System.out.println(pcsfTail.bestForest.nodesInF.get(test++));
			}
			if (verboseLevel > 0) {
				System.out.println("number of head nodes : " + pcsfHead.bestForest.nodesInF.size());
				System.out.println("number of tail nodes : " + pcsfTail.bestForest.nodesInF.size());
			}
			/** they are not equal */
			resultNodes_Tail = Utils.getIntArrayFromIntegerList(pcsfTail.bestForest.nodesInF);
		}
		resultNodes_supportX = getSupportNodes(x);
		funcValue = function.getFuncValue(x);
		runTime = (System.nanoTime() - startTime) / 1e9;
		//System.out.print("Test array x");
		//System.out.println(Arrays.toString(x));
		
		return x;
	}

	/**
	 * in order to fit the gradient, we need to normalize the gradient if the
	 * domain of function is within [0,1]^n
	 * 
	 * @param x
	 *            input vector x
	 * @param gradient
	 *            gradient vector
	 * @return normGradient the normalized gradient
	 */
	private double[] normalizeGradient(double[] x, double[] gradient) {
		double[] normalizedGradient = new double[graphSize];
		for (int i = 0; i < graphSize; i++) {
			if ((gradient[i] < 0.0D) && (x[i] == 0.0D)) {
				normalizedGradient[i] = 0.0D;
			} else if ((gradient[i] > 0.0D) && (x[i] == 1.0D)) {
				normalizedGradient[i] = 0.0D;
			} else {
				normalizedGradient[i] = gradient[i];
			}
		}
		return normalizedGradient;
	}

	private double[] initializeX_Random() {
		double[] x0 = new double[c.length];
		Random rand = new Random();
		for (int i = 0; i < c.length; i++) {
			if (rand.nextDouble() < 0.5D) {
				x0[i] = 1.0D;
			} else {
				x0[i] = 0.0D;
			}
		}
		return x0;
	}

	private double[] initializeX_MaximumCC() {

		ArrayList<ArrayList<Integer>> adj = new ArrayList<>();
		for (int i = 0; i < nodes.size(); i++) {
			adj.add(new ArrayList<>());
		}
		for (Integer[] edge : this.edges) {
			adj.get(edge[0]).add(edge[1]);
			adj.get(edge[1]).add(edge[0]);
		}

		ConnectedComponents cc = new ConnectedComponents(adj);
		int[] abnormalNodes = null;
		double mean = StatUtils.mean(c);
		double std = Math.sqrt(StatUtils.variance(c));
		for (int i = 0; i < this.c.length; i++) {
			if (Math.abs(c[i]) >= mean + std) {
				abnormalNodes = ArrayUtils.add(abnormalNodes, i);
			}
		}
		cc.computeCCSubGraph(abnormalNodes);
		int[] largestCC = cc.findLargestConnectedComponet(abnormalNodes);
		double[] x0 = new double[this.c.length];
		for (int i = 0; i < x0.length; i++) {
			x0[i] = 0.0D;
		}
		for (int i : largestCC) {
			x0[i] = 1.0D;
		}
		return x0;
	}

	private double[] initializeX_RandomSingleNode() {
		/** this is for others */
		int[] abnormalNodes = null;
		for (int i = 0; i < nodes.size(); i++) {
			if (c[i] == 0.0D) {
				abnormalNodes = ArrayUtils.add(abnormalNodes, i);
			}
		}
		if (abnormalNodes == null) {
			abnormalNodes = new int[] { 0 };
		}
		int index = new Random().nextInt(abnormalNodes.length);
		double[] x0 = new double[this.c.length];
		for (int i = 0; i < x0.length; i++) {
			x0[i] = 0.0D;
		}
		x0[abnormalNodes[index]] = 1.0D;
		return x0;
	}

	/**
	 * union two sets s1 and s2
	 *
	 * @param s1
	 *            set s1
	 * @param s2
	 *            set s2
	 * @return the union of two sets
	 */
	private ArrayList<Integer> unionSets(ArrayList<Integer> s1, ArrayList<Integer> s2) {
		if (s2 == null) {
			return s1;
		}
		for (Integer i : s2) {
			if (!s1.contains(i)) {
				s1.add(i);
			}
		}
		return s1;
	}

	/**
	 * get a support of a vector
	 *
	 * @param x
	 *            array x
	 * @return a subset of nodes corresponding the index of vector x with
	 *         entries not equal to zero
	 */
	public ArrayList<Integer> support(double[] x) {
		if (x == null) {
			return null;
		}
		ArrayList<Integer> nodes = new ArrayList<>();
		for (int i = 0; i < x.length; i++) {
			if (x[i] != 0.0D) {
				nodes.add(i);
			}
		}
		return nodes;
	}

	/**
	 * get the nodes returned by algorithm
	 *
	 * @param x
	 *            array x
	 * @return the result nodes
	 */
	private int[] getSupportNodes(double[] x) {
		int[] nodes = null;
		for (int i = 0; i < x.length; i++) {
			if (x[i] != 0.0D) {
				nodes = ArrayUtils.add(nodes, i); // get nonzero nodes
			}
		}
		return nodes;
	}
}
