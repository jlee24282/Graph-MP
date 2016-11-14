package edu.albany.cs.base;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;


public class GenerateRandomData {
	int n;
	int numTrueNodes;
	double p1;	//percentage for the normal nodes
	double p2;	//percentage for the abnormal nodes
	double c;	//constant for the nodes weight

	public GenerateRandomData(int n, int numTrueNodes, double p1, double p2, double c){
		this.n = n;
		this.numTrueNodes = numTrueNodes;
		this.p1 = p1;
		this.p2 = p2;
		this.c = c;
        double[] pValue 		= new double[n];
	}

	//generate graph for the p1, p2 and c
	//precision/ recall/ fmeasure
	private void generateGraph(){

	}

	private int randomNode(int nodeId, double[] pValue) {
        Random R 			= new Random();

		double p = R.nextDouble();
		double cumulativeProbability = 0.0;
		int item = -1;

		for (int i = 0; i< pValue.length; i++) {
			cumulativeProbability += pValue[i];
			if (p <= cumulativeProbability && i != nodeId) {
				item = i;
			}
		}

		return item;
	}

	public void generate_data_random(String outputFilePath) {
		int min_edge 		= 5; 	//minimum edge from one node
		int numTrueNodes 	= 30;
		double alpha		= 0.05;
		Random R 			= new Random();
		int prevRnd = 0;
		outputFilePath  	= outputFilePath+min_edge+"_TrueNode_"+ numTrueNodes+ ".txt";
		System.out.println("New File Path: " + outputFilePath);
		
		/** step0: data file */
	    double[] pValue 		= new double[n];
	    double[] counts 		= new double[n];
	    double[] userWeight 	= new double[n];
		ArrayList<Edge> edges 			= new ArrayList<Edge>();
	    HashMap<Integer, Double> nodes 	= new HashMap<Integer, Double>() ;
	    
	    int[] trueNodes 							= genRandTrueNodes(numTrueNodes, n);
	    HashMap<int[], Double> trueSubGraphEdges 	= genRandTrueEdges(trueNodes);

	    /** step1: set pValue*/
		for (int i = 0; i < n; i++){
			if(ArrayUtils.contains(trueNodes, i))
				pValue[i] = p2;
			else
				pValue[i] = p1;
		}

	    
		/** step2: Generate Nodes with weight*/
		for (int i = 0; i < n; i++){
			nodes.put(i, (double) i);
			double rnd = R.nextDouble();
			if(pValue[i] == p2) { // if abnormal
                userWeight[i] = rnd + c;
                if(userWeight[i] >= 1)
                    userWeight[i] = 1;
            }
			else
				userWeight[i] = rnd;
		}
		
		
		/** step3: Generate Edges */
		for (int node1 = 0; node1< n; node1++){
			for(int j = 0; j< min_edge; j++){

				int node2 = randomNode(node1, pValue);

				Edge e = new Edge(node1, node2, 0, 1.0);

				Edge reE = new Edge(node2, node1, 0, 1.0);



				if (edges.contains(e))
					System.out.println("TEst1111.");

				if (!edges.contains(e)) {
					edges.add(e);

					Edge e2 = new Edge(node2, node1, 0, 1.0);
					if (reE.equals(e2))
						System.out.println("TEst.");
				}
				if (!edges.contains(reE)) {
					edges.add(reE);
				}
			}
		}

		Collections.sort(edges, new Comparator<Edge>() {
		    @Override
		    public int compare(Edge e2, Edge e1)
		    {
		        return  e2.i.compareTo(e1.i);
		    }
		});
		
		/** step3: Generate File */
		APDMInputFormat.generateAPDMFile_bot("Test", "RandomData", edges, userWeight,
				counts,  trueSubGraphEdges,  outputFilePath);

		System.out.println("------------------------------ File generated --------------------------------\n");	
	}
	
	private int[] genRandTrueNodes(int t_size, int n_size){
		ArrayList<Integer> t_nodes = new ArrayList<Integer>();
		Random R 		= new Random();
		int rnd 		= 0;
		while(t_nodes.size() <= t_size){
			 rnd = R.nextInt(n_size);
			 if(!t_nodes.contains(rnd)){
				 t_nodes.add(rnd);
			 }
		}

		int[] tNodes 	= ArrayUtils.toPrimitive(t_nodes.toArray(new Integer[0]));
		System.out.println("TrueNode: " + ArrayUtils.toString(tNodes));
		return tNodes;
	}
	
	private HashMap<int[], Double> genRandTrueEdges(int[] trueNodes){
		HashMap<int[], Double> tEdge = new HashMap<int[], Double>();
		
		int i = 0;
		
		while(tEdge.size() < trueNodes.length-1){
			int[] key = new int[]{trueNodes[i], trueNodes[i+1]};i++;
			tEdge.put(key, 1.0);
		}
		return tEdge;
	}
	
	public static void main(String args[]) {
		new GenerateRandomData(
				50 /*Node size*/,
				30/*True node size*/,
				0.2/*p1*/,
				0.4/*p1*/,
				0.5/*c*/).generate_data_random("data/BotData/APDM-min_edge_");
	}
}
