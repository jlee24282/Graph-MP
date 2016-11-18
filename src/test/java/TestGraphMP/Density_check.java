package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.PreRec;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.Cd_detection;

import java.util.ArrayList;
import java.util.Arrays;


//dense detection
public class Density_check {

    private int verboseLevel = 0;

    public void test_cd_detection(String inputFilePath) {
        System.out.println("\n------------------------------ test starts ------------------------------");
        System.out.println("testing file path: " + inputFilePath);
        /** step0: data file */
        APDMInputFormat apdm = new APDMInputFormat(inputFilePath);

        ArrayList<Integer[]> edges = apdm.data.intEdges;
        ArrayList<Double> edgeCosts = apdm.data.identityEdgeCosts;
        /** step1: score function */
        //graphWeightedAdjList
        //System.out.println(apdm.data.graphWeightedAdjList);
        Cd_detection func = new Cd_detection(apdm.data.graphWeightedAdjList);

//		System.out.print(apdm.data.graphAdjList.get(0));
//		Utils.stop();
        /** step2: optimization */
        int sparsity = 0;
        int[] candidateS = new int[] {    13, 14, 15, 16};
        //int[] candidateS = new int[] {   5, 6, 7, 8, 9};
        double optimalVal = -Double.MAX_VALUE;
        PreRec bestPreRec = new PreRec();
        GraphMP bestGraphMP = null;
        for (int s : candidateS) {
            double B = s - 1 + 0.0D;
            int t = 5;
            GraphMP graphMP = new GraphMP(edges, edgeCosts, apdm.data.base, s, 1, B, t, false/** maximumCC */
                    , null, func, null);
            bestPreRec = new PreRec(graphMP.resultNodes_supportX, apdm.data.trueSubGraphNodes);
            System.out.println(bestPreRec.toString()+" "+func.getFuncValue(graphMP.x));
            System.out.println("Sparsity : " + s );
            double[] yx = graphMP.x;
            if (func.getFuncValue(yx) > optimalVal) {
                optimalVal = func.getFuncValue(yx);
                bestGraphMP = graphMP;
                sparsity = s;
                if (verboseLevel == 0) {
                    System.out.println("current best [pre,rec]: " + "[" + bestPreRec.pre + "," + bestPreRec.rec + "]");
                }
            }
        }
        System.out.println("precision : " + bestPreRec.pre + " ; recall : " + bestPreRec.rec);
        System.out.println("Sparsity : " + sparsity );
        System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
        System.out.println("true subgraph is: " + Arrays.toString(apdm.data.trueSubGraphNodes));
        System.out.println("------------------------------ test ends --------------------------------\n");
    }

    public static void main(String args[]) {
        new Cd_detection_test().test_cd_detection("data/BotData/APDM-0.1_0.9_c0.5_TrueNode30.txt");
        //new Cd_detection_test().test_cd_detection("data/DensedData/APDM-GridData-100_noise_0.0_trueSubSize_30_4");
        //new TestToyExample().testToyExample("data/SimulationData/Protest/APDM-GridData-100-precen_0.1-noise_0-numCC_1_0.txt");
        //new TestToyExample().testToyExample("data/GridDataEBP/APDM-GridData-100_noise_0.0_trueSubSize_30_0.txt");
    }
}
