package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.GenerateRandomData;
import edu.albany.cs.base.PreRec;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.Cd_detection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.albany.cs.base.GenerateRandomData;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;


public class DensityCheck extends Application {

    private int verboseLevel = 1;

    public void test_cd_detection(String variable, String inputFilePath) {
        System.out.println("\n------------------------------" + variable + " test starts ------------------------------");
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
            //System.out.println(bestPreRec.toString()+" "+func.getFuncValue(graphMP.x));
            //System.out.println("Sparsity : " + s );
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
        //System.out.println("Sparsity : " + sparsity );
        System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
        System.out.println("true subgraph is: " + Arrays.toString(apdm.data.trueSubGraphNodes));
        System.out.println("------------------------------ test ends --------------------------------\n");
    }

    @Override public void start(Stage stage) {
        stage.setTitle("p1=0.2, p2=0.4");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("c");
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setAnimated(false);
        lineChart.setTitle("p1: 0.2, p2: 0.4");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Precision");

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Recall");

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("fMeasure");z

        series1.getData().add(new XYChart.Data(0.1, 23));
        series1.getData().add(new XYChart.Data(0.2, 14));
        series1.getData().add(new XYChart.Data(0.3, 15));
        series1.getData().add(new XYChart.Data(0.4, 24));
        series1.getData().add(new XYChart.Data(0.5, 34));
        series1.getData().add(new XYChart.Data(0.6, 36));
        series1.getData().add(new XYChart.Data(0.7, 22));
        series1.getData().add(new XYChart.Data(0.8, 45));
        series1.getData().add(new XYChart.Data(0.9, 43));

        series2.getData().add(new XYChart.Data(0.1, 33));
        series2.getData().add(new XYChart.Data(0.2, 34));
        series2.getData().add(new XYChart.Data(0.3, 25));
        series2.getData().add(new XYChart.Data(0.4, 44));
        series2.getData().add(new XYChart.Data(0.5, 39));
        series2.getData().add(new XYChart.Data(0.6, 16));
        series2.getData().add(new XYChart.Data(0.7, 55));
        series2.getData().add(new XYChart.Data(0.8, 54));
        series2.getData().add(new XYChart.Data(0.9, 48));

        series3.getData().add(new XYChart.Data(0.1, 44));
        series3.getData().add(new XYChart.Data(0.2, 35));
        series3.getData().add(new XYChart.Data(0.3, 36));
        series3.getData().add(new XYChart.Data(0.4, 33));
        series3.getData().add(new XYChart.Data(0.5, 31));
        series3.getData().add(new XYChart.Data(0.6,  26));
        series3.getData().add(new XYChart.Data(0.7, 22));
        series3.getData().add(new XYChart.Data(0.8, 25));
        series3.getData().add(new XYChart.Data(0.9, 43));

        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().addAll(series1, series2, series3);

        stage.setScene(scene);
        stage.show();
        saveAsPng(lineChart, "chart1.png");
    }

    public void saveAsPng(LineChart lineChart, String path) {
        WritableImage image = lineChart.snapshot(new SnapshotParameters(), null);
        File file = new File(path);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void densityCheckGraph(String args[]){
        int nodeSize = 50;
        int trueNodeSize = 30;
        double p1 = 0.2;
        double p2 = 0.4;
        double c = 0.5;

        /*************************
         * Test c
         *************************/
        for(int i = 1; i< 10; i++){
            c = i/10.0;
            //new GenerateRandomData( nodeSize, trueNodeSize, p1, p2, c).generate_data_random("data/BotData/APDM");
            new DensityCheck().test_cd_detection("c", "data/BotData/APDM-" + p1 + "_" + p2 + "_c" + c + "_TrueNode30.txt");
        }
        launch(args);

        /*************************
         * Test p1
         *************************/
        p2 = 0.4;
        c = 0.5;
        for(int i = 1; i< 10; i++){
            p1 = i/10.0;
            //new GenerateRandomData( nodeSize, trueNodeSize, p1, p2, c).generate_data_random("data/BotData/APDM");
            new DensityCheck().test_cd_detection("p1", "data/BotData/APDM-" + p1 + "_" + p2 + "_c" + c + "_TrueNode30.txt");
        }
        //launch(args);

        /*************************
         * Test p2
         *************************/
        p1 = 0.2;
        c = 0.5;
        for(int i = 1; i< 10; i++){
            p2 = i/10.0;
           // new GenerateRandomData( nodeSize, trueNodeSize, p1, p2, c).generate_data_random("data/BotData/APDM");
            new DensityCheck().test_cd_detection("p2", "data/BotData/APDM-" + p1 + "_" + p2 + "_c" + c + "_TrueNode30.txt");
        }
        //launch(args);
    }

    public static void main(String args[]) {
        densityCheckGraph(args);
    }
}