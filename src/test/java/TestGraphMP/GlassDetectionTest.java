package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.PreRec;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.ToyFunc;

import java.util.ArrayList;
import java.util.Arrays;


public class GlassDetectionTest {

    private int verboseLevel = 0;

    public void testToyExample(String inputFilePath) {
        System.out.println("\n------------------------------ test starts ------------------------------");
        System.out.println("testing file path: " + inputFilePath);
        /** step0: data file */
        APDMInputFormat apdm = new APDMInputFormat(inputFilePath);

        ArrayList<Integer[]> edges = apdm.data.intEdges;
        ArrayList<Double> edgeCosts = apdm.data.identityEdgeCosts;
        /** step1: score function */

        //GlassDetection func = new GlassDetection(apdm.data.PValue);
        ToyFunc func = new ToyFunc(apdm.data.PValue);

//		Utils.stop();
        /** step2: optimization */
        int[] candidateS = new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        double optimalVal = - Double.MAX_VALUE;
        PreRec bestPreRec = new PreRec();
        GraphMP bestGraphMP = null;
        for (int s : candidateS) {
            double B = s - 1 + 0.0D;
            int t = 5;
            GraphMP graphMP = new GraphMP(edges, edgeCosts, apdm.data.base, s, 1, B, t, false/** maximumCC */
                    , null, func, null);
            bestPreRec = new PreRec(graphMP.resultNodes_supportX, apdm.data.trueSubGraphNodes);
            //System.out.println(bestPreRec.toString()+" "+func.getFuncValue(graphMP.x));
            double[] yx = graphMP.x;
            if (func.getFuncValue(yx) > optimalVal) {
                optimalVal =  func.getFuncValue(yx);

                bestGraphMP = graphMP;
                if (verboseLevel == 0) {
                    System.out.println("current best [pre,rec]: " + "[" + bestPreRec.pre + "," + bestPreRec.rec + "]");
                }
            }
        }
        System.out.println("precision : " + bestPreRec.pre + " ; recall : " + bestPreRec.rec);
        System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
        System.out.println("true subgraph is: " + Arrays.toString(apdm.data.trueSubGraphNodes));
        System.out.println("------------------------------ test ends --------------------------------\n");
    }

    public static void main(String args[]) {
        new GlassDetectionTest().testToyExample("data/PixelData/APDM-10X11_C100.0_trueSubSize_30.txt");
        //new GlassDetectionTest().testToyExample("data/GridData/APDM-GridData-100_noise_0.0_trueSubSize_30_4.txt");
        //new TestToyExample().testToyExample("data/SimulationData/Protest/APDM-GridData-100-precen_0.1-noise_0-numCC_1_0.txt");
        //new GlassDetectionTest().testToyExample("data/GridDataEBP/APDM-GridData-100_noise_0.0_trueSubSize_30_0.txt");
    }
}
