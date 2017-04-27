package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.PixelToAPDMData;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.GlassDetection;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GlassDetectionTest implements Runnable {
    public static final int DOWNSIZENUM = 4;
    public static String NAME = "at33";
    public static int PICINDEX = 0;
    public static int NeighborNum = 7;
    private int verboseLevel = 0;
    public int[] candidateS = new int[] {   2, 3, 4, 5, 6, 7, 8,  9, 10, 11, 12, 13, 14, 15};


    public void gdTest(String inputFilePath) throws IOException{
        System.out.println("\n------------------------------ test starts ------------------------------");
        System.out.println("testing file path: " + inputFilePath);
        /** step0: data file */
        APDMInputFormat apdm = new APDMInputFormat(inputFilePath);

        ArrayList<Integer[]> edges = apdm.data.intEdges;
        ArrayList<Double> edgeCosts = apdm.data.identityEdgeCosts;
        /** step1: score function */
        GlassDetection func = new GlassDetection(apdm.data.greyValues, PICINDEX);

        /** step2: optimization */
        double optimalVal = Double.MAX_VALUE;
        GraphMP bestGraphMP = null;
        int bestPicture = -1;
        FileWriter fileWriter = null;

        String text = inputFilePath.toString();
        Pattern p = Pattern.compile(Pattern.quote("APDM-") + "(.*?)" + Pattern.quote("-4-30X32.txt"));
        Matcher m = p.matcher(text);
        while (m.find()) {
            NAME = m.group(1);
        }
        System.out.println(NAME);

        try{
            fileWriter = new FileWriter("data/PixelData/RealData/Down"+DOWNSIZENUM+"_KNN_"+NeighborNum+"/ResultData/"+ NAME + "_"+DOWNSIZENUM + "_"+ PICINDEX, true);
            fileWriter.write("\n------------------------------ test starts ------------------------------ \n");
            fileWriter.close();
        }catch(IOException e){
            e.printStackTrace();
        }

        for (int s : candidateS) {
            fileWriter = new FileWriter("data/PixelData/RealData/Down"+DOWNSIZENUM+"_KNN_"+NeighborNum+"/ResultData/"+ NAME + "_"+DOWNSIZENUM + "_"+ PICINDEX, true);
//
            double B = s - 1 + 0.0D;
            int t = 5;
//            fileWriter.write("test2");
            GraphMP graphMP = new GraphMP(edges, edgeCosts, apdm.data.base, s, 1, B, t, false/** maximumCC */
                    , null, func, null);
            System.out.println("s**********************************************: " + s);
            fileWriter.write("s**********************************************: " + s+ "\n");
            System.out.println("Current result: " + ArrayUtils.toString(graphMP.resultNodes_Tail));
            fileWriter.write("Current result: " + ArrayUtils.toString(graphMP.resultNodes_Tail) + "\n");
            System.out.println("Current function value: " + graphMP.funcValue);
            fileWriter.write("Current function value: " + graphMP.funcValue+ "\n");
            System.out.println(func.getFuncValue(graphMP.x) + " "+ ArrayUtils.toString(graphMP.x)+ "\n");
            double[] yx = graphMP.x;
            if (func.getFuncValue(yx) < optimalVal && graphMP.resultNodes_Tail[0] != graphMP.x.length-1) {
                optimalVal = func.getFuncValue(yx);
                bestPicture = func.getPicIndex();
                bestGraphMP = graphMP;
            }
            fileWriter.close();
        }

        fileWriter = new FileWriter("data/PixelData/RealData/Down"+DOWNSIZENUM+"_KNN_"+NeighborNum+"/ResultData/"+ NAME + "_"+DOWNSIZENUM + "_"+ PICINDEX, true);
        System.out.println("sRESULT**********************************************: ");
        fileWriter.write("sRESULT**********************************************: "+ "\n");
        System.out.println("Picture Index: " + bestPicture);
        System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
        fileWriter.write("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail)+ "\n");
        System.out.println("------------------------------ test ends --------------------------------\n");
        fileWriter.write("------------------------------ test ends --------------------------------\n");

        //displayResultPicture(bestGraphMP.resultNodes_Tail);
        fileWriter.close();
    }
    public void run() {

        final long startTime = System.currentTimeMillis();
        for(int i = 0; i< 1; i++) {
            PICINDEX = 0;
            try {
                if (NAME.contains("test")) {
                    new GlassDetectionTest().gdTest("data/PixelData/RealData/APDM/KNN_" + NeighborNum + "/APDM-" + NAME + ".txt");
                } else {
                    if (DOWNSIZENUM == 2)
                        new GlassDetectionTest().gdTest("data/PixelData/RealData/APDM/KNN_" + NeighborNum + "/APDM-" + NAME + "-" + DOWNSIZENUM + "-60X64.txt");
                    if (DOWNSIZENUM == 4) {
                        String inputDir = "data/PixelData/RealData/APDM/KNN_" + NeighborNum;
                        File directory = new File(inputDir);

                        // get all the files from a directory
                        File[] fList = directory.listFiles();
                        for (File file : fList) {
                            if (file.isFile() && !file.toString().contains(".DS_Store")) {
                                new GlassDetectionTest().gdTest(file.toString());
                            }
                        }
                    }
                }
            }
            catch (IOException e) {
                System.err.println("Couldn't close input stream");
            }

            //int[] data = {781, 782, 783, 813, 815, 816, 833, 845, 865, 877, 897, 898, 899, 900, 901, 902, 903, 904, 905, 906, 907, 908, 909, 928, 929, 930};
            //new GlassDetectionTest().displayResultPicture(data);
        }
        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime));
    }


    public static void main(String args[]) throws IOException{
    // *** Main Thread ***
        GlassDetectionTest gd = new GlassDetectionTest();
        Thread[] threads = new Thread[gd.candidateS.length];
        for(int i = 0; i < 2; i++) {
            threads[i] = new Thread(gd);
            threads[i].start();
        }

        for(int i = 0; i < 2; i++) {
            try {
                threads[i].join();}
            catch(InterruptedException e){
                System.out.println("error");}
        }
    }


    private void displayResultPicture(int[] resultGraph) throws IOException{

        int[] imagePixel = new int[100];
        String inputFiles = "data/PixelData/RealData/Images/ImageData/pngFiles/faces/"+NAME+"/";
        PixelToAPDMData pd = new PixelToAPDMData();
        imagePixel = pd.getGreyLevelsFromImages(inputFiles +NAME+ "_straight_neutral_sunglasses_" + DOWNSIZENUM+ ".png");
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
            File f = new File("data/PixelData/RealData/Results/Output_" +NAME+"_"+ DOWNSIZENUM+ ".png");
            BufferedImage resultImage = inputImage;
            resultImage.setRGB(0, 0, pd.getWidth(), pd.getHeight(), imagePixel, 0, pd.getWidth());
            ImageIO.write(resultImage, "png", f);
            System.out.println("CREATED");
        }catch(IOException e){
            System.out.println(e);
        }
    }
}
