package edu.albany.cs.base;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static java.lang.System.exit;


public class PixelToAPDMData {
    private int PIC_HEIGHT;
    private int PIC_WIDTH;
    private int PIXEL_COUNT;
    private int PICTURE_COUNT;
    private double[][] greyValues;
    private BufferedImage img;

    public PixelToAPDMData(){
        //temporary for now
        PICTURE_COUNT = 12;
        img = null;
    }

    public void generateSingleCase(String inputFiles, String outputFile) throws IOException {
        this.greyValues     = getGreyLevels(inputFiles);

        outputFile = outputFile + PIC_HEIGHT +"X"+ PIC_WIDTH + ".txt";
        generatePixelAPDM(outputFile);

        //test if all connected
        testTrueSubGraph(outputFile);
    }

    private double[][] getGreyLevels(String inputFiles) throws IOException{
        //an2i_left_neutral_open_2.png;
        int[][] greyValuesT = new int[100][100];

        greyValuesT[0] = getGreyLevelsFromImages(inputFiles + "an2i_straight_neutral_sunglasses_4.png");
        greyValuesT[1] = getGreyLevelsFromImages(inputFiles + "an2i_straight_neutral_open_4.png");
        greyValuesT[2] = getGreyLevelsFromImages(inputFiles + "an2i_straight_sad_open_4.png");
        greyValuesT[3] = getGreyLevelsFromImages(inputFiles + "an2i_straight_angry_open_4.png");
        greyValuesT[4] = getGreyLevelsFromImages(inputFiles + "an2i_straight_happy_open_4.png");
        greyValuesT[5] = getGreyLevelsFromImages(inputFiles + "an2i_left_happy_open_4.png");
        greyValuesT[6] = getGreyLevelsFromImages(inputFiles + "an2i_left_angry_open_4.png");
        greyValuesT[7] = getGreyLevelsFromImages(inputFiles + "an2i_left_neutral_open_4.png");
        greyValuesT[8] = getGreyLevelsFromImages(inputFiles + "an2i_left_sad_open_4.png");
        greyValuesT[9] = getGreyLevelsFromImages(inputFiles + "an2i_right_happy_open_4.png");
        greyValuesT[10] = getGreyLevelsFromImages(inputFiles + "an2i_right_sad_open_4.png");
        greyValuesT[11] = getGreyLevelsFromImages(inputFiles + "an2i_right_neutral_open_4.png");

        greyValues = new double[PIXEL_COUNT][PICTURE_COUNT];

        for(int i = 0; i< 12; i++){
            for (int k = 0; k< PIXEL_COUNT; k++){
                greyValues[k][i] = greyValuesT[i][k];
            }
        }

        return greyValues;
    }

    public int[] getGreyLevelsFromImages(String inputFile) throws IOException{
        //get picture and get rgb

        try {
            img = ImageIO.read(new FileInputStream(inputFile));
        } catch (IOException e) {
            System.out.println(e);
            exit(0);
        }

        PIC_HEIGHT = img.getHeight();
        PIC_WIDTH = img.getWidth();
        PIXEL_COUNT = (int)(Math.ceil(PIC_HEIGHT) * Math.ceil(PIC_WIDTH));
        int[] grayLevels = new int[PIXEL_COUNT];
        Arrays.fill(grayLevels,0);

        //convert to grayscale
        int[] imageRGB = img.getRGB(0, 0, PIC_WIDTH, PIC_HEIGHT, null, 0, PIC_WIDTH);
        int[] tempImageCheck = new int[PIXEL_COUNT];
        for(int i = 0; i < PIC_HEIGHT * PIC_WIDTH; i++){
            int p = imageRGB[i];
            tempImageCheck[i] = p;

    //        int a 	= (imageRGB[i] >> 16) & 0xff;
            int r 	= (imageRGB[i] >> 16) & 0xff;
            int g 	= (imageRGB[i] >> 8) & 0xff;
            int b 	= (imageRGB[i]) & 0xff;
            //calculate average(grayVal)
            grayLevels[i] = (r + g + b) / 3;
            r = (r + g + b) / 3;
            g = r;
            b = r;

            tempImageCheck[i] = r << 16 | g << 8 | b;
        }

//        try{
//            File f = new File("data/Output.png");
//            BufferedImage resultImage = img;
//            resultImage.setRGB(0, 0, PIC_WIDTH, PIC_HEIGHT, tempImageCheck, 0, PIC_WIDTH);
//            ImageIO.write(resultImage, "png", f);
//            System.out.println("CREATED");
//        }catch(IOException e){
//            System.out.println(e);
//        }


        //checking output
        return grayLevels;
    }


    private void generatePixelAPDM(String outputFileName){
        //gen edges
        GenerateMNSingleGraph g = new GenerateMNSingleGraph(PIC_HEIGHT, PIC_WIDTH);
        //nothing in count
        double[] count          = new double[PIXEL_COUNT];
        Arrays.fill(count, 0.0);

        APDMInputFormat.generateAPDMFilePixel("Test", "PixelData", g.edges, this.greyValues,
                count,  null,  outputFileName);
    }


    public void testTrueSubGraph(String fileName) {
        APDMInputFormat apdm = new APDMInputFormat(fileName);
        //System.out.println(apdm.data.trueSubGraphNodes.length);
        ConnectedComponents cc = apdm.data.cc;
        System.out.println(cc.computeCCSubGraph(apdm.data.trueSubGraphNodes));
    }

    public static void main(String args[]) throws IOException{
        new PixelToAPDMData().generateSingleCase(
                "data/PixelData/RealData/Images/ImageData/pngFiles/faces/an2i/",
                "data/PixelData/RealData/APDM/APDM-an2i-4-");
    }

    public BufferedImage getBufferedImage(){
        return img;
    }

    public int getWidth(){
        return PIC_WIDTH;
    }

    public int getHeight(){
        return PIC_HEIGHT;
    }
}
