package edu.albany.cs.base;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;


public class GenerateMNSingleGraph {

    final int numOfNodes ;
    final ArrayList<ArrayList<Integer>> adj ;
    final double[][] adjMatrix ;
    final int[][] adjInt ;
    final ArrayList<Edge> edges ;
    final int M;
    final int N;

    GenerateMNSingleGraph(int M, int N){
        this.M      = M;
        this.N      = N;
        numOfNodes  = M * N;
        adj         = this.generateGraph() ;
        adjMatrix   = this.generateGraph(1.0) ;
        adjInt      = this.generateGraph(false) ;
        edges       = new ArrayList<Edge>() ;
        int count = 0 ;
        double weight = 1.0 ;
        for(int i = 0 ; i < adj.size() ; i++){
            for( int j = 0 ; j < adj.get(i).size() ; j ++){
                edges.add(new Edge(i,adj.get(i).get(j),count++,weight)) ;
            }
        }
    }

    private double[][] generateGraph(double weight){
        double[][] graph = new double[M*N][M*N] ;
        for(int k=0;k<graph.length;k++){
            for(int j=0;j<graph.length;j++){
                graph[k][j] = -1 ;
            }
        }

        int current = 0;
        for (int i = 0; i < M*N; i++){
            for(int j = 0; j< M*N; j++) {
                if(i != j) {
                    if (j - 1 >= 0 && !(j % N == 0)) {
                        System.out.println("j-1" + current + " " + " ");
                        graph[i][j - 1] = weight;
                    }
                    if (j + 1 < M * N && !(j % N == N - 1)) {
                        System.out.println("j+1" + current+ " " + " ");
                        graph[i][j + 1] = weight;
                    }
                    if (j - N >= 0 && !(j % N == 0)) {
                        System.out.println("j-N" + current+ " " + " ");
                        graph[i][j - N] = weight;
                    }
                    if (j + N < M * N && !(j % N == N - 1)) {
                        System.out.println("j+N" + current+ " " + " ");
                        graph[i][j + N] = weight;
                    }
                }
                current++;
                /*
                if (i != j) {
                    //top edge
                    if(current < N){
                        //Right top corner
                        if(current%N == N-1){
                            System.out.println("RightTopCorner" + current);
                            graph[i][j-1] = weight;
                            graph[i][j+N] = weight;
                        //topedge
                        }else{
                            System.out.println("TopEdge" + current);
                            graph[i][j-1] = weight;
                            graph[i][j+1] = weight;
                            graph[i][j+N] = weight;
                        }
                    }
                    //down edge
                    else if(current >= M*N*M*N - M*N){
                        //down left corner
                        if(current%N == 0){
                            System.out.println("DownLeftCorner" + current);
                            graph[i][j+1] = weight;
                            graph[i][j-N] = weight;
                        }//down edge
                        else{
                            System.out.println("DownEdge" + current + i + j);
                            graph[i][j-1] = weight;
                            graph[i][j+1] = weight;
                            graph[i][j-N] = weight;

                        }
                    }
                    //inthe middle
                    else{
                        System.out.println("middle" + current);

                        //middle left edge
                        if(current%N == 0){
                            System.out.println("Middle left edge" + current);
                            graph[i][j + 1] = weight;
                            if(j-N >= 0)
                                graph[i][j - N] = weight;
                            if(j+N < M*N)
                                graph[i][j + N] = weight;
                        }//middle right edge
                        else if(current%N == N-1){
                            System.out.println("middle right edge" + current + i + j);
                            graph[i][j - 1] = weight;
                            graph[i][j - N] = weight;
                            if(j+N < M*N)
                                graph[i][j + N] = weight;
                        }
                        else{
                            graph[i][j - 1] = weight;
                            graph[i][j + 1] = weight;
                            if(j-N >= 0)
                                graph[i][j - N] = weight;
                            if(j+N < M*N)
                                graph[i][j + N] = weight;
                        }

                    }
                }*/
            }
        }

        return graph;
    }

    private ArrayList<ArrayList<Integer>> generateGraph(){
        double[][] graph                    = generateGraph(1.0);
        ArrayList<ArrayList<Integer>> adj   = new ArrayList<ArrayList<Integer>>() ;

        for(int j = 0 ; j < graph.length ; j ++){
            ArrayList<Integer> arr = new ArrayList<Integer>() ;
            for( int k = 0 ; k < graph[j].length ; k ++){
                if(graph[j][k] > 0){
                    arr.add(k) ;
                }
            }
            adj.add(arr) ;
        }
        return adj ;
    }

    private int[][] generateGraph(boolean flag){
        double[][] graph = generateGraph(1.0);
        int[][] adj = new int[N*N][] ;
        for(int j = 0 ; j < graph.length ; j ++){
            int[] arr = null ;
            for( int k = 0 ; k < graph[j].length ; k ++){
                if(graph[j][k] > 0){
                    arr = ArrayUtils.add(arr, k) ;
                }
            }
            adj[j] = arr ;
        }
        return adj ;
    }

    public static void main(String args[]){
    }

}
