package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.PixelToAPDMData;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.GlassDetection;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class GlassDetectionTest {
    public static final int downsizenum = 4;
    public static final String NAME = "saavik";
    private int verboseLevel = 0;

    public void testToyExample(String inputFilePath) throws IOException{
        System.out.println("\n------------------------------ test starts ------------------------------");
        System.out.println("testing file path: " + inputFilePath);
        /** step0: data file */
        APDMInputFormat apdm = new APDMInputFormat(inputFilePath);

        ArrayList<Integer[]> edges = apdm.data.intEdges;
        ArrayList<Double> edgeCosts = apdm.data.identityEdgeCosts;

        /** step1: score function */
        GlassDetection func = new GlassDetection(apdm.data.greyValues);

        /** step2: optimization */
        int[] candidateS = new int[] { 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22};
        double optimalVal =  Double.MAX_VALUE;
        GraphMP bestGraphMP = null;
        int bestPicture = -1;
        for (int s : candidateS) {
            double B = s - 1 + 0.0D;
            int t = 5;
            GraphMP graphMP = new GraphMP(edges, edgeCosts, apdm.data.base, s, 1, B, t, false/** maximumCC */
                    , null, func, null);
            System.out.println("s**********************************************: " + s);
            System.out.println("Current result: " + ArrayUtils.toString(graphMP.resultNodes_supportX));
            System.out.println("Current function value: " + graphMP.funcValue);
            System.out.println(func.getFuncValue(graphMP.x) + " "+ ArrayUtils.toString(graphMP.x));
            double[] yx = graphMP.x;
            if (func.getFuncValue(yx) < optimalVal) {
                optimalVal = func.getFuncValue(yx);
                bestPicture = func.getPicIndex();
                bestGraphMP = graphMP;
            }
        }
        System.out.println("sRESULT**********************************************: ");
        System.out.println("Picture Index: " + bestPicture);
        System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
        System.out.println("------------------------------ test ends --------------------------------\n");

        displayResultPicture(bestGraphMP.resultNodes_Tail);

    }

    private void displayResultPicture(int[] resultGraph) throws IOException{

        int[] imagePixel = new int[100];
        String inputFiles = "data/PixelData/RealData/Images/ImageData/pngFiles/faces/"+NAME+"/";
        PixelToAPDMData pd = new PixelToAPDMData();
        imagePixel = pd.getGreyLevelsFromImages(inputFiles +NAME+ "_straight_neutral_sunglasses_" + downsizenum+ ".png");
        BufferedImage inputImage = pd.getBufferedImage();

        for(int i : resultGraph){
            int alpha = 0;
            int red   = 255;
            int green = 255;
            int blue  = 255;

            int argb = alpha << 24 + red << 16 + green << 8 + blue;

            //imagePixel[i] = -5921371;
            imagePixel[i] = 16711680;
        }

        try{
            File f = new File("data/PixelData/RealData/Results/Output_" +NAME+"_"+ downsizenum+ ".png");
            BufferedImage resultImage = inputImage;
            resultImage.setRGB(0, 0, pd.getWidth(), pd.getHeight(), imagePixel, 0, pd.getWidth());
            ImageIO.write(resultImage, "png", f);
            System.out.println("CREATED");
        }catch(IOException e){
            System.out.println(e);
        }
    }

    public static void main(String args[]) throws IOException{
        if (downsizenum == 2)
            new GlassDetectionTest().testToyExample("data/PixelData/RealData/APDM/APDM-"+NAME+"-" + downsizenum+ "-60X64.txt");
        if (downsizenum == 4)
            new GlassDetectionTest().testToyExample("data/PixelData/RealData/APDM/APDM-"+NAME+"-" + downsizenum+ "-30X32.txt");
        //int[] data = {781, 782, 783, 813, 815, 816, 833, 845, 865, 877, 897, 898, 899, 900, 901, 902, 903, 904, 905, 906, 907, 908, 909, 928, 929, 930};
        //new GlassDetectionTest().displayResultPicture(data);
    }
}
