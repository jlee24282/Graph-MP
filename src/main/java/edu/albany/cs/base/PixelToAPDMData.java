package edu.albany.cs.base;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.exit;


public class PixelToAPDMData {
    public static final String NAME = "";
    public static final int downsize = 2;
    public static String Z0NAME = "";
    private int PIC_HEIGHT;
    private int PIC_WIDTH;
    private int PIXEL_COUNT;
    private int PICTURE_COUNT;
    private double[][] greyValues;
    private BufferedImage img;

    public PixelToAPDMData() {
        //temporary for now
        PICTURE_COUNT = 0;
        img = null;
    }

    public void generateSingleCase(String inputFiles, String outputFile) throws IOException {
        this.greyValues = getGreyLevels(inputFiles);

        outputFile = outputFile + PIC_HEIGHT + "X" + PIC_WIDTH + ".txt";
        generatePixelAPDM(outputFile);

        //test if all connected
        testTrueSubGraph(outputFile);
    }

    private double[][] getGreyLevels(String inputDir) throws IOException {
        //an2i_left_neutral_open_2.png;
        int[][] greyValuesT = new int[PICTURE_COUNT][960];

        ArrayList<File> files = new ArrayList<File>();
        File directory = new File(inputDir);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
                increasePICTURE_COUNT();
            }
        }
        System.out.println(getPICTURE_COUNT());

        greyValuesT[0] = getGreyLevelsFromImages(inputDir + NAME + "_straight_neutral_sunglasses_" + downsize + ".png");
        greyValuesT[1] = getGreyLevelsFromImages(inputDir + NAME + "_straight_neutral_open_" + downsize + ".png");
        greyValuesT[2] = getGreyLevelsFromImages(inputDir + NAME + "_straight_sad_open_" + downsize + ".png");
        greyValuesT[3] = getGreyLevelsFromImages(inputDir + NAME + "_straight_angry_open_" + downsize + ".png");
        greyValuesT[4] = getGreyLevelsFromImages(inputDir + NAME + "_up_happy_open_" + downsize + ".png");
        greyValuesT[5] = getGreyLevelsFromImages(inputDir + NAME + "_up_neutral_open_" + downsize + ".png");
        greyValuesT[6] = getGreyLevelsFromImages(inputDir + NAME + "_left_happy_open_" + downsize + ".png");
        greyValuesT[7] = getGreyLevelsFromImages(inputDir + NAME + "_left_neutral_open_" + downsize + ".png");
        greyValuesT[8] = getGreyLevelsFromImages(inputDir + NAME + "_left_sad_open_" + downsize + ".png");
        greyValuesT[9] = getGreyLevelsFromImages(inputDir + NAME + "_right_happy_open_" + downsize + ".png");
        greyValuesT[10] = getGreyLevelsFromImages(inputDir + NAME + "_right_sad_open_" + downsize + ".png");
        greyValuesT[11] = getGreyLevelsFromImages(inputDir + NAME + "_right_neutral_open_" + downsize + ".png");


        greyValues = new double[PIXEL_COUNT][PICTURE_COUNT];

        for (int i = 0; i < 12; i++) {
            for (int k = 0; k < PIXEL_COUNT; k++) {
                greyValues[k][i] = greyValuesT[i][k];
            }
        }

        return greyValues;
    }

    public int[] getGreyLevelsFromImages(String inputFile) throws IOException {
        //get picture and get rgb

        try {
            img = ImageIO.read(new FileInputStream(inputFile));
        } catch (IOException e) {
            System.out.println(e);
            exit(0);
        }

        PIC_HEIGHT = img.getHeight();
        PIC_WIDTH = img.getWidth();
        PIXEL_COUNT = (int) (Math.ceil(PIC_HEIGHT) * Math.ceil(PIC_WIDTH));
        int[] grayLevels = new int[PIXEL_COUNT];
        System.out.println(PIXEL_COUNT);
        Arrays.fill(grayLevels, 0);

        //convert to grayscale
        int[] imageRGB = img.getRGB(0, 0, PIC_WIDTH, PIC_HEIGHT, null, 0, PIC_WIDTH);
        int[] tempImageCheck = new int[PIXEL_COUNT];
        for (int i = 0; i < PIC_HEIGHT * PIC_WIDTH; i++) {
            int p = imageRGB[i];
            tempImageCheck[i] = p;

            //        int a 	= (imageRGB[i] >> 16) & 0xff;
            int r = (imageRGB[i] >> 16) & 0xff;
            int g = (imageRGB[i] >> 8) & 0xff;
            int b = (imageRGB[i]) & 0xff;
            //calculate average(grayVal)
            //grayLevels[i] = (r + g + b) / 3;
            grayLevels[i] = p;
            r = (r + g + b) / 3;
            g = r;
            b = r;

            tempImageCheck[i] = r;//((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);
            grayLevels[i] = tempImageCheck[i];
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


    private void generatePixelAPDM(String outputFileName) {
        //gen edges
        GenerateMNSingleGraph g = new GenerateMNSingleGraph(PIC_HEIGHT, PIC_WIDTH);
        //nothing in count
        double[] count = new double[PIXEL_COUNT];
        Arrays.fill(count, 0.0);

        APDMInputFormat.generateAPDMFilePixel("Test", "PixelData", g.edges, this.greyValues,
                count, null, outputFileName);
    }


    public void testTrueSubGraph(String fileName) {
        APDMInputFormat apdm = new APDMInputFormat(fileName);
        //System.out.println(apdm.data.trueSubGraphNodes.length);
        ConnectedComponents cc = apdm.data.cc;
        System.out.println(cc.computeCCSubGraph(apdm.data.trueSubGraphNodes));
    }

    public static void main(String args[]) throws IOException {

        if (NAME.contains("test")) {
            new PixelToAPDMData().generateSingleCase(
                    "data/PixelData/RealData/Images/ImageData/pngFiles/test/",
                    "data/PixelData/RealData/APDM/APDM-" + NAME);
        } else if (NAME.equals("")) {
            Z0NAME = "an2i";
            new PixelToAPDMData().generateSingleCase(
                    "data/PixelData/RealData/Images/ImageData/pngFiles/",
                    "data/PixelData/RealData/APDM/APDM-" + Z0NAME + "-" + downsize + "-");
        } else {
            new PixelToAPDMData().generateSingleCase(
                    "data/PixelData/RealData/Images/ImageData/pngFiles/faces/" + NAME + "/",
                    "data/PixelData/RealData/APDM/APDM-" + NAME + "-" + downsize + "-");
        }
    }

    public BufferedImage getBufferedImage() {
        return img;
    }

    public int getWidth() {
        return PIC_WIDTH;
    }

    public int getHeight() {
        return PIC_HEIGHT;
    }

    public int getPICTURE_COUNT(){return PICTURE_COUNT;}
    public void increasePICTURE_COUNT(){ PICTURE_COUNT = PICTURE_COUNT+1;}
}
