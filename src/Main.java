import java.util.ArrayList;
import java.util.Random;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jiri
 */
public class Main {
    static Random rnd = new Random();
    static int popSize;
    static int popSizeFinalEA;
    static int poolSizeFinalEA;
    static int nbPerturb;
    static int minGen, maxGen;
    static int maxFinalEAGenerations;
    static double totalTime;
    static short nbDesert;
    static int seed;

    public static void main(String[] args) throws Exception{
        Main.rnd.setSeed(2);
        ReadData rd = new ReadData("data\\" + args[0]);
        System.out.println("Reading data ...\n");

        //--- read data
        long startTime = System.currentTimeMillis();
        rd.readSimple();
        long endTime = System.currentTimeMillis();
        
        if(ReadData.nbZones <= 20){
            popSize = 100;
            popSizeFinalEA = 100;
            poolSizeFinalEA = 150;
            minGen = 20;
            maxGen = 100;
            maxFinalEAGenerations = 100;
            nbPerturb = 5;
            nbDesert = 2;
            totalTime = 2.9;
        }
        else if(ReadData.nbZones <= 100){
            popSize = 30;
            popSizeFinalEA = 50;
            poolSizeFinalEA = 100;
            minGen = 5;
            maxGen = 40;
            maxFinalEAGenerations = 100;
            nbPerturb = 10;
            nbDesert = 2;
            totalTime = 4.95;
        }
        else if(ReadData.nbZones <= 150){
            popSize = 50;
            popSizeFinalEA = 50;
            poolSizeFinalEA = 80;
            minGen = 1;
            maxGen = 50;
            maxFinalEAGenerations = 50;     //--- the more generations the better results
            nbPerturb = 20;
            nbDesert = 10;
            totalTime = 14.95;
        }
        else{
            popSize = 40;
            popSizeFinalEA = 40;
            poolSizeFinalEA = 60;
            minGen = 1;
            maxGen = 50;
            maxFinalEAGenerations = 40;
            nbPerturb = 30;
            nbDesert = 2;
            totalTime = 14.95;
        }

        //--- EA
        EA ea = new EA(popSize);
        Individual best = ea.run(endTime, totalTime-(endTime-startTime)/1000.0);
        endTime = System.currentTimeMillis();
        best.printSolution();
    }    
}
