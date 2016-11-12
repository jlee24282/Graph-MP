package edu.albany.cs.fastPCST;

import edu.albany.cs.headApprox.PCSFHead;
import edu.albany.cs.headApprox.PCSFHead.F;

import java.util.ArrayList;

/**
 * FastPCSF this algorithm is based on FastPCST algorithm
 *
 * @author baojian bzhou6@albany.edu
 */
public class FastPCSF {

    public F f;
    public ArrayList<Integer[]> subTreeEdges = new ArrayList<Integer[]>();
    public ArrayList<Double> subTreeCosts = new ArrayList<Double>();


    public FastPCSF(ArrayList<Integer[]> edges, ArrayList<Double> edgeCosts, ArrayList<Double> pi, int g) {

        FastPCST pcstFast = new FastPCST(edges, pi, edgeCosts, FastPCST.kNoRoot, g, FastPCST.PruningMethod.kStrongPruning, -1);
        ArrayList<Integer> result_nodes = null;
        ArrayList<Integer> result_edges = null;
        if (!pcstFast.run(result_edges, result_nodes)) {
            System.out.println("Error : Algorithm returned false. There must be an error. \n");
            System.exit(0);
        } else {
            result_edges = pcstFast.result_edges; //index
            result_nodes = pcstFast.result_nodes;
        }
        ArrayList<Integer[]> subTreeEdges = new ArrayList<Integer[]>();
        ArrayList<Double> subTreeCosts = new ArrayList<Double>();
        for (int i : pcstFast.result_edges) {
            subTreeEdges.add(new Integer[]{edges.get(i)[0], edges.get(i)[1]});
            subTreeCosts.add(edgeCosts.get(i));
        }
        ArrayList<Double> prizePi = new ArrayList<Double>();
        for (int i : result_nodes) {
            prizePi.add(pi.get(i));
        }
        double totalPrizes = 0.0D;
        for (int i = 0; i < pi.size(); i++) {
            totalPrizes += pi.get(i);
        }
        PCSFHead pcsfHead = new PCSFHead();
        f = pcsfHead.new F(result_nodes, subTreeEdges, subTreeCosts, prizePi, totalPrizes);
    }


}
