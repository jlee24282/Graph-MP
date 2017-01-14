package edu.albany.cs.base;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.System.exit;


public class PixelToAPDMData {
    private int PIC_ROWS;
    private int PIC_COLUMNS;
    private int PIXEL_COUNT;
    private int PICTURE_COUNT;
    private double[][] greyValues;

    public PixelToAPDMData(){
        //temporary for now
        PICTURE_COUNT = 12;

    }

    public void generateSingleCase(String inputFiles, String outputFile) throws IOException {
        this.greyValues     = getGreyLevelsFromImages(inputFiles);
        this.PIC_COLUMNS = greyValues[0].length;
        this.PIC_ROWS = greyValues.length;
        this.PIXEL_COUNT    = PIC_COLUMNS * PIC_ROWS;

        outputFile = outputFile + PIC_ROWS +"X"+ PIC_COLUMNS + ".txt";
        generatePixelAPDM(outputFile);

        //test if all connected
        testTrueSubGraph(outputFile);
    }

    private double[][] getGreyLevelsFromImages(String inputFile) throws IOException{
        //get picture and get rgb
        BufferedImage img = null;

        try {
            //img = ImageIO.read(new FileInputStream("test-p1.png"));
            img = ImageIO.read(new FileInputStream(inputFile + "/siggi.gif"));
        } catch (IOException e) {
            System.out.println(e);
            exit(0);
        }

        PIC_ROWS = img.getHeight();
        PIC_COLUMNS = img.getWidth();
        PIXEL_COUNT = PIC_ROWS * PIC_COLUMNS;
        double[][] grayLevels = new double[PIXEL_COUNT][PICTURE_COUNT];
        for(int i = 0; i< PIXEL_COUNT; i++)
            Arrays.fill(grayLevels[i],0.0);

        //convert to grayscale
        int k = 0;
        for(int y = 0; y < PIC_ROWS; y++){
            for(int x = 0; x < PIC_COLUMNS; x++){
                int p = img.getRGB(x,y);

                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                //calculate average(grayVal)
                grayLevels[k++][0] = (r+g+b)/3;
            }
        }

        return grayLevels;
    }


    private void generatePixelAPDM(String outputFileName){
        //gen edges
        GenerateMNSingleGraph g = new GenerateMNSingleGraph(PIC_ROWS, PIC_COLUMNS);
        //nothing in count
        double[] count          = new double[PIXEL_COUNT];
        Arrays.fill(count, 0.0);

        APDMInputFormat.generateAPDMFilePixel("Test", "PixelData", g.edges, this.greyValues,
                count,  null,  outputFileName);
    }


    public void testTrueSubGraph(String fileName) {
        APDMInputFormat apdm = new APDMInputFormat(fileName);
        System.out.println(apdm.data.trueSubGraphNodes.length);
        ConnectedComponents cc = apdm.data.cc;
        System.out.println(cc.computeCCSubGraph(apdm.data.trueSubGraphNodes));
    }

    public static void main(String args[]) throws IOException{
        new PixelToAPDMData().generateSingleCase(
                "data/PixelData/RealData/Images",
                "data/PixelData/RealData/APDM-");
    }

}
