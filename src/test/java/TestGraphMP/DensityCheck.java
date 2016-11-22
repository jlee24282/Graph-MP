package TestGraphMP;

import edu.albany.cs.base.APDMInputFormat;
import edu.albany.cs.base.GenerateRandomData;
import edu.albany.cs.base.PreRec;
import edu.albany.cs.graphMP.GraphMP;
import edu.albany.cs.scoreFuncs.Cd_detection;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class DensityCheck extends Application {

    private int verboseLevel = 1;
    private int xSize = 10;
    private double[] pre ;
    private double[] recall;
    private double[] fmeasure;

    public DensityCheck() {
        pre  = new double[xSize];
        recall = new double[xSize];
        fmeasure = new double[xSize];
    }

    public PreRec testDensityCheck(String variable, String inputFilePath) {
        if(verboseLevel == 0)
            System.out.println("\n------------------------------" + variable + " test starts ------------------------------");
        System.out.println("testing file path: " + inputFilePath);
        /** step0: data file */
        APDMInputFormat apdm = new APDMInputFormat(inputFilePath);

        ArrayList<Integer[]> edges = apdm.data.intEdges;
        ArrayList<Double> edgeCosts = apdm.data.identityEdgeCosts;
        /** step1: score function */
        //graphWeightedAdjList
        //System.out.println(apdm.data.graphWeightedAdjList);
        Cd_detection func = new Cd_detection(apdm.data.graphAdjList);

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
        if(verboseLevel == 0) {
            System.out.println("precision : " + bestPreRec.pre + " ; recall : " + bestPreRec.rec);
            //System.out.println("Sparsity : " + sparsity );
            System.out.println("result subgraph is: " + Arrays.toString(bestGraphMP.resultNodes_Tail));
            System.out.println("true subgraph is: " + Arrays.toString(apdm.data.trueSubGraphNodes));
            System.out.println("------------------------------ test ends --------------------------------\n");
        }

        return bestPreRec;
    }

    @Override public void start(Stage stage) {
        stage.setTitle("p1=0.2, p2=0.4");
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);
        lineChart.setAnimated(false);
        lineChart.setTitle("p1: 0.2, p2: 0.4");

        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Precision");

        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Recall");

        XYChart.Series series3 = new XYChart.Series();
        series3.setName("fMeasure");
        //----------------------------------------------

        String testVariable ="p1";
        densityCheckGraph(this,testVariable);
        xAxis.setLabel(testVariable);

        for(int i = 0; i<xSize; i++)
            series1.getData().add(new XYChart.Data((i+1)/10.0, this.pre[i]));
        for(int i = 0; i<xSize; i++)
            series2.getData().add(new XYChart.Data((i+1)/10.0, this.recall[i]));
        for(int i = 0; i<xSize; i++)
            series3.getData().add(new XYChart.Data((i+1)/10.0, this.fmeasure[i]));

        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().addAll(series1, series2, series3);

        stage.setScene(scene);
        //stage.show();
        saveAsPng(lineChart, "test-"+testVariable+ ".png");

        //----------------------------------------------

         testVariable ="p2";

        densityCheckGraph(this,testVariable);
        xAxis.setLabel(testVariable);
        lineChart.getData().removeAll(series1, series2, series3);
         series1 = new XYChart.Series();
        series1.setName("Precision");

         series2 = new XYChart.Series();
        series2.setName("Recall");

        series3 = new XYChart.Series();
        series3.setName("fMeasure");

        for(int i = 0; i<xSize; i++)
            series1.getData().add(new XYChart.Data((i+1)/10.0, this.pre[i]));
        for(int i = 0; i<xSize; i++)
            series2.getData().add(new XYChart.Data((i+1)/10.0, this.recall[i]));
        for(int i = 0; i<xSize; i++)
            series3.getData().add(new XYChart.Data((i+1)/10.0, this.fmeasure[i]));

         //scene  = new Scene(lineChart,800,600);
        lineChart.getData().addAll(series1, series2, series3);
        stage.setScene(scene);
        saveAsPng(lineChart, "test-"+testVariable+ ".png");
        System.out.println("------------------------------ Fin --------------------------------\n");

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

    public void densityCheckGraph(DensityCheck dc, String variable){
        int nodeSize = 100;
        int trueNodeSize = 30;
        double p1 = 0.2;
        double p2 = 0.4;
        double c = 0.5;

        PreRec bestPreRec = new PreRec();

        if(variable.equals("c")){

            /*************************
             * Test c
             *************************/
            for(int i = 0; i< xSize; i++){
                c = (i+1)/10.0;
                //new GenerateRandomData( nodeSize, trueNodeSize, p1, p2, c).generate_data_random("data/BotData/APDM");
                bestPreRec = dc.testDensityCheck("c", "data/BotData/APDM-" + p1 + "_" + p2 + "_c" + c + "_TrueNode30.txt");
                this.fmeasure[i] = bestPreRec.fmeasure;
                this.pre[i] = bestPreRec.pre;
                this.recall[i] = bestPreRec.rec;
            }
        }
        else if(variable.equals("p1")){
            /*************************
             * Test p1
             *************************/
            p2 = 0.4;
            c = 0.5;
            for(int i = 0; i< xSize; i++){
                p1 = (i+1)/10.0;
                new GenerateRandomData( nodeSize, trueNodeSize, p1, p2, c).generate_data_random("data/BotData/APDM");
                bestPreRec = dc.testDensityCheck("p1", "data/BotData/APDM-" + p1 + "_" + p2 + "_c" + c + "_TrueNode30.txt");
                this.fmeasure[i] = bestPreRec.fmeasure;
                this.pre[i] = bestPreRec.pre;
                this.recall[i] = bestPreRec.rec;
                System.out.println(this.fmeasure[i]+" "+ this.pre[i]+" "+ this.recall[i]);
            }
        }
        else if(variable.equals("p2")){
            /*************************
             * Test p2
             *************************/
            p1 = 0.2;
            c = 0.5;
            for(int i = 0; i< xSize; i++){
                p2 = (i+1)/10.0;
                new GenerateRandomData( nodeSize, trueNodeSize, p1, p2, c).generate_data_random("data/BotData/APDM");
                bestPreRec = dc.testDensityCheck("p2", "data/BotData/APDM-" + p1 + "_" + p2 + "_c" + c + "_TrueNode30.txt");
                this.fmeasure[i] = bestPreRec.fmeasure;
                this.pre[i] = bestPreRec.pre;
                this.recall[i] = bestPreRec.rec;
                System.out.println(this.fmeasure[i]+" "+ this.pre[i]+" "+ this.recall[i]);
            }
        }
    }

    public static void main(String args[]) {
        launch(args);
    }
}