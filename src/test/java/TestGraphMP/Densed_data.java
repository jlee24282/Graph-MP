package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;


public class Densed_data {

	public void generate_data(String inputFilePath) {
		int max_edge = 1000;
		
		System.out.println("testing file path: " + inputFilePath);
		/** step0: data file */
		APDMInputFormat apdm 		= new APDMInputFormat(inputFilePath);
		int[] true_subgraph_nodes 	= apdm.data.trueSubGraphNodes;
		ArrayList<Edge> new_edges 	= apdm.data.newEdges;
		Random R = new Random();
		Edge e = new_edges.get(0);
		Edge re_e = new_edges.get(0);
		
		/** step2: Generate Edges */
		for (int new_node: true_subgraph_nodes){
			for (int i = 0; i<max_edge; i++){
				
				while( new_edges.contains(e) ){
					int rnd = R.nextInt(true_subgraph_nodes.length);
					if(true_subgraph_nodes[rnd] == new_node)
						rnd = (rnd+1) % true_subgraph_nodes.length;
						
					e 		= new Edge(new_node, true_subgraph_nodes[rnd], 0, 1.0);
					re_e 	= new Edge(true_subgraph_nodes[rnd], new_node, 0, 1.0);
				}
				
				new_edges.add(e);
				if (new_edges.contains(re_e))
					new_edges.add(re_e);
				
			}
		}

		Collections.sort(new_edges, new Comparator<Edge>() {
		    @Override
		    public int compare(Edge e2, Edge e1)
		    {
		        return  e2.i.compareTo(e1.i);
		    }
		});
		
		
		/** step2: Generate File */

		APDMInputFormat.generateAPDMFile("Test", "GridData", new_edges, apdm.data.PValue,
				apdm.data.PValue, apdm.data.base,  apdm.data.trueSubGraphEdges,  "data/DensedData/APDM-GridData-100_noise_0.0_trueSubSize_30_4");

		System.out.println("------------------------------ File generated --------------------------------\n");
			
	}

	public static void main(String args[]) {
		new Densed_data().generate_data("data/GridData/APDM-GridData-100_noise_0.0_trueSubSize_30_4.txt");
		//new Densed_data().generate_data("data/BotData/APDM-min_edge_7_TrueNode_10.txt");
	}
}
