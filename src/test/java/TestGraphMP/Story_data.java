package TestGraphMP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.Edge;


public class Story_data {
	// Sorting
	
	public void generate_data(String outputFilePath) {
		int k 				= 6;				// clique size
		int clique_amount 	= 3;				// clique amount
		int edge_in_clique 	= k*(k-1)/2;		// edges in clique
		int n 				= k*clique_amount - clique_amount +1; 	// total node size 
		Random R = new Random();

		outputFilePath  = outputFilePath+ k + "k_" + clique_amount + "clique" +".txt";
		System.out.println("New File Path: " + outputFilePath);
		
		/** step0: data file */
		ArrayList<Edge> edges 		= new ArrayList<Edge>();
		ArrayList<Integer> used_n 	= new ArrayList<Integer>();
		HashMap<int[], Double> trueSubGraphEdges = null;
	    double[] base =  new double[n];
	    double[] counts = new double[n];
	    double[] PValue = new double[n];
	    HashMap<Integer, Double> nodes = new HashMap<Integer, Double>() ;
	    int random = 0;
	    
		/** step1: Generate Nodes with weight*/
		for (int i = 0; i < n; i++){
			nodes.put(i, (double) i);
			double rnd = R.nextDouble();
			PValue[i] = rnd * 31;
		}
		/** generate main nodes*/
		ArrayList<Integer> sig_nodes = new ArrayList<Integer>();   //significant nodes
		while (sig_nodes.size() < clique_amount+1) {
		    random = R.nextInt(n);
		    if (!sig_nodes.contains(random)) {
		    	sig_nodes.add(random);
		    	used_n.add(random);
		    }
		}
		System.out.println(sig_nodes);
		/** step2: Generate Edges*/
		for (int i = 0; i< clique_amount; i++){
			ArrayList<Integer> single_clq = new ArrayList<Integer>();  //nodes in single clique 
			//Edge e = new Edge(sig_nodes.get(i), sig_nodes.get(i + 1), 0, 1.0);
			//edges.add(e);
			single_clq.add(sig_nodes.get(i));
			single_clq.add(sig_nodes.get(i+1));
			while (single_clq.size() < k) {
			    random = R.nextInt(n);
			    if (!single_clq.contains(random) && !used_n.contains(random)) {
			    	single_clq.add(random);
			    	used_n.add(random);
			    }
			}
			System.out.println(single_clq);
			for (int x = 0; x< k; x++){
				for (int y = x; y < k; y++){
					Edge e = new Edge(single_clq.get(x), single_clq.get(y), 0, 1.0);
					Edge re_e = new Edge(single_clq.get(y), single_clq.get(x), 0, 1.0);
					if (!edges.contains(e) && !edges.contains(re_e) && x != y)
						edges.add(e);
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
		APDMInputFormat.generateAPDMFile("Test", "Story_data", edges, PValue,
				counts,  trueSubGraphEdges,  outputFilePath);

		System.out.println("------------------------------ File generated --------------------------------\n");	
	}
	
	public void generate_data_random(String outputFilePath) {
		int n 	 = 10; 	// total node size 
		int min_edge = 6; 	//minimum edge from one node
		Random R = new Random();
		outputFilePath  = outputFilePath+min_edge+".txt";
		System.out.println("New File Path: " + outputFilePath);
		
		/** step0: data file */
		ArrayList<Edge> edges 						= new ArrayList<Edge>();
		HashMap<int[], Double> trueSubGraphEdges 	= null;
	    double[] base 	= new double[n];
	    double[] counts = new double[n];
	    double[] PValue = new double[n];
	    HashMap<Integer, Double> nodes = new HashMap<Integer, Double>() ;

		/** step1: Generate Nodes with weight*/
		for (int i = 0; i < n; i++){
			nodes.put(i, (double) i);
			double rnd = R.nextDouble();
			PValue[i] = rnd * 31;
		}
		
		/** step2: Generate Edges */
		for (int i = 0; i< n; i++){
			for (int j = 0; j< min_edge; j++){
				int rnd = R.nextInt(n);
				Edge e = new Edge(i, rnd, 0, 1.0); 
				
				while(edges.contains(e))
					rnd = R.nextInt(n);
					e = new Edge(i, rnd, 0, 1.0);

				edges.add(e);
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
		APDMInputFormat.generateAPDMFile("Test", "Story_data", edges, PValue,
				counts,  trueSubGraphEdges,  outputFilePath);

		System.out.println("------------------------------ File generated --------------------------------\n");	
	}
	
	public static void main(String args[]) {
		new Story_data().generate_data_random("data/Story_telling/APDM-min_edge_");
		new Story_data().generate_data("data/Story_telling/APDM-");
	}
}
