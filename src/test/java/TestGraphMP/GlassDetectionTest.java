package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.PreRec;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.GlassDetection;

import java.util.ArrayList;
import java.util.Arrays;


public class GlassDetectionTest {

    private int verboseLevel = 10;

    public void testToyExample(String inputFilePath) {
        System.out.println("\n------------------------------ test starts ------------------------------");
        System.out.println("testing file path: " + inputFilePath);
        /** step0: data file */
        APDMInputFormat apdm = new APDMInputFormat(inputFilePath);

        ArrayList<Integer[]> edges = apdm.data.intEdges;
        ArrayList<Double> edgeCosts = apdm.data.identityEdgeCosts;

        /** step1: score function */
        GlassDetection func = new GlassDetection(apdm.data.greyValues);

        /** step2: optimization */
        int[] candidateS = new int[] {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        double optimalVal = - Double.MAX_VALUE;
        PreRec bestPreRec = new PreRec();
        GraphMP bestGraphMP = null;
        int bestPicture = -1;
        for (int s : candidateS) {
            double B = s - 1 + 0.0D;
            int t = 5;
            GraphMP graphMP = new GraphMP(edges, edgeCosts, apdm.data.base, s, 1, B, t, false/** maximumCC */
                    , null, func, null);
            bestPreRec = new PreRec(graphMP.resultNodes_supportX, apdm.data.trueSubGraphNodes);
            //System.out.println(bestPreRec.toString()+" "+func.getFuncValue(graphMP.x));
            double[] yx = graphMP.x;
            if (func.getFuncValue(yx) > optimalVal) {
                optimalVal = func.getFuncValue(yx);
                bestPicture = func.getPicIndex();
                //System.out.println(func.getQ());
                bestGraphMP = graphMP;
                if (verboseLevel == 0) {
                    System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
                    System.out.println("current best [pre,rec]: " + "[" + bestPreRec.pre + "," + bestPreRec.rec + "]");
                }
            }
        }
        System.out.println("Picture Index: " + bestPicture);
        System.out.println("precision : " + bestPreRec.pre + " ; recall : " + bestPreRec.rec);
        System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
        System.out.println("true subgraph is: " + Arrays.toString(apdm.data.trueSubGraphNodes));
        System.out.println("------------------------------ test ends --------------------------------\n");
    }

    public static void main(String args[]) {
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbLow/APDM-10X11_C10.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbLow/APDM-10X11_C50.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbLow/APDM-10X11_C100.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbLow/APDM-10X11_C150.0_trueSubSize_30.txt");

//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbHigh/APDM-10X11_C10.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbHigh/APDM-10X11_C50.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbHigh/APDM-10X11_C100.0_trueSubSize_30.txt");
        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbHigh/APDM-10X10_C150.0_trueSubSize_30.txt");
        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbHigh/APDM-10X10_C200.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/AbHigh/APDM-10X11_C300.0_trueSubSize_30.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/APDM-10X11_C200.0_trueSubSize_30_1.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/APDM-10X11_C200.0_trueSubSize_30_2.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/APDM-10X11_C200.0_trueSubSize_30_3.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/APDM-10X11_C200.0_trueSubSize_30_4.txt");
//        new GlassDetectionTest().testToyExample("data/PixelData/SimulationData/APDM-10X11_C200.0_trueSubSize_30_5.txt");
    }
}
