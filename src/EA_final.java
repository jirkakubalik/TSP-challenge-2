
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jiri
 */
public class EA_final {
    ArrayList<Individual> population;
    ArrayList<Individual> initialPop;
    int popSize;
    Individual bestSoFar;
    int bestFitness;
    int worstFitness;
    
    public EA_final(ArrayList<Individual> inds){
        bestFitness = Integer.MAX_VALUE;
        worstFitness = 0;
        initialPop = inds;
        popSize = initialPop.size();
        population = new ArrayList<>();
        for(int i=0; i<initialPop.size(); i++){
            population.add(initialPop.get(i).copyConstructor());
            for(short lsIter=0; lsIter<500; lsIter++){
                if(Main.rnd.nextDouble() < 0.8){
                    population.get(i).saOpSwap(lsIter, i, population.get(i).fitness+50, population.get(i).fitness-50);
                }
                else{
                    population.get(i).saOpTripleSwap(lsIter, i, population.get(i).fitness+50, population.get(i).fitness-50);
                }
            }
            if(bestFitness > population.get(i).fitness){
                bestFitness = population.get(i).fitness;
            }
            if(worstFitness < population.get(i).fitness){
                worstFitness = population.get(i).fitness;
            }
            System.out.print(population.get(i).fitness + ":");
            for(int j=0; j<population.get(i).tripZones.length; j++){
                System.out.print("\t" + population.get(i).tripAirports[j]);
            }
            System.out.println("");
        }
        bestSoFar = population.get(getBest()).copyConstructor();
        bestSoFar.calculateTripPrice(true);
        bestSoFar.checkFitness();
        System.out.print(bestSoFar.fitness + ":");
        for(int j=0; j<bestSoFar.tripZones.length; j++){
            System.out.print("\t" + bestSoFar.tripAirports[j]);
        }
        System.out.println("");
        //---
        System.out.println("Best eaFinal initial: " + bestSoFar.fitness);
    }
    
    public Individual run(int maxIter) throws Exception{
        int gen = 0;
        int tourn1 = 4;
        int tourn2 = 1;
        double pCross = 0.9;
        double pM = 0.1;
        double p1 = 0.75;
        
        ArrayList<Individual> tempPop = new ArrayList<>();
        while(gen < maxIter){
            long startTime = System.currentTimeMillis();
            //---
            tempPop.clear();
            while(tempPop.size() < Main.popSizeFinalEA){
                Individual par1 = population.get(tournament(tourn1)).copyConstructor();
                Individual par2 = population.get(tournament(tourn2)).copyConstructor();
                Individual child; 
                if(Main.rnd.nextDouble() < pCross){
                    if(Main.rnd.nextDouble() < p1){
                        child = par1;
                        child.eaFinal_crossover2point(par2);
                    }
                    else{
                        child = par2;
                        child.eaFinal_crossover2point(par1);
                    }
                    if(Main.rnd.nextDouble() < pM){
                        child.eaFinal_mutate();
                    }
                }
                else{
                    if(Main.rnd.nextDouble() < p1){
                        child = par1;
                    }
                    else{
                        child = par2;
                    }
                    child.eaFinal_mutate();
                }
                tempPop.add(child);
            }

            //--- Evaluate new individuals - use multiple threads
            int nbThreads = 3;
            int batchSize = tempPop.size() / nbThreads;
            CountDownLatch latch = new CountDownLatch(nbThreads);
            EAFinalEvalThread[] eaFinalThreads = new EAFinalEvalThread[nbThreads];
            for(int nT=0; nT<nbThreads; nT++){
                int first = nT * batchSize;
                int last;
                if(nT == nbThreads-1){
                    last = tempPop.size() - 1;
                }
                else{
                    last = (nT+1) * batchSize;
                }
                eaFinalThreads[nT] = new EAFinalEvalThread(tempPop, first, last, latch);
                new Thread(eaFinalThreads[nT]).start();
            }
            latch.await(); // Wait for countdown        
            //---
            Collections.sort(tempPop, Individual.compByFitness);
            
            //--- Fine-tuning: tempPop
            //--- Fine-tuning - the best individual
            for(short lsIter=0; lsIter<200; lsIter++){
                if(Main.rnd.nextDouble() < 0.9){
                    tempPop.get(0).saOpSwap(lsIter, gen, tempPop.get(0).fitness, tempPop.get(0).fitness);
                }
                else{
                    tempPop.get(0).saOpTripleSwap(lsIter, gen, tempPop.get(0).fitness, tempPop.get(0).fitness);
                }
            }
            //--- Fine-tuning - random individual
            for(short lsIter=0; lsIter<200; lsIter++){
                for(int j=1; j<10 && j<tempPop.size(); j++){
                    if(Main.rnd.nextDouble() < 0.9){
                        tempPop.get(j).saOpSwap(lsIter, gen, tempPop.get(j).fitness, tempPop.get(j).fitness);
                    }
                    else{
                        tempPop.get(j).saOpTripleSwap(lsIter, gen, tempPop.get(j).fitness, tempPop.get(j).fitness);
                    }
                }
            }

            //--- Fine-tuning: population
            //--- Fine-tuning - the best individual
            for(short lsIter=0; lsIter<100; lsIter++){
                if(Main.rnd.nextDouble() < 0.9){
                    population.get(0).saOpSwap(lsIter, gen, population.get(0).fitness, population.get(0).fitness);
                }
                else{
                    population.get(0).saOpTripleSwap(lsIter, gen, population.get(0).fitness, population.get(0).fitness);
                }
            }
            //--- Fine-tuning - random individual
            for(short lsIter=0; lsIter<200; lsIter++){
                for(int j=1; j<10 && j<population.size(); j++){
                    if(Main.rnd.nextDouble() < 0.9){
                        population.get(j).saOpSwap(lsIter, gen, population.get(j).fitness, population.get(j).fitness);
                    }
                    else{
                        population.get(j).saOpTripleSwap(lsIter, gen, population.get(j).fitness, population.get(j).fitness);
                    }
                }
            }
            //---
            tempPop.addAll(population);
            Collections.sort(tempPop, Individual.compByFitness);
            population.clear();
            population.add(tempPop.get(0));
            int i = 1;
            //--- take unique solutions
            while(i < tempPop.size() && population.size() < Main.popSizeFinalEA){
                if(!tempPop.get(i).isEqualTo(population.get(population.size()-1))){
                    population.add(tempPop.get(i));
                }
                i+=1;   //--- every second
            }
            //---
            System.out.println(gen + ".\t" + population.get(getBest()).fitness);
            gen++;
        }

        //--- print final population
        System.out.println("\nFinal population");
        for(int i=0; i<population.size(); i++){
            System.out.print(population.get(i).fitness + ":");
            for(int j=0; j<population.get(i).tripZones.length; j++){
                System.out.print("\t" + population.get(i).tripAirports[j]);
            }
            System.out.println("");
        }
        
        bestSoFar = population.get(getBest());
        bestSoFar.checkFitness();
        return bestSoFar;
    }

    public int tournament(int n){
        int best = Main.rnd.nextInt(population.size());
        int k = 1;
        while(k < n){
            int cand = Main.rnd.nextInt(population.size());
            if(population.get(best).fitness > population.get(cand).fitness){
                best = cand;
            }
            k++;
        }
        return best;
    }
    
    void printPopStat(){
        System.out.println("");
        System.out.println("  bestFitness = " + bestFitness);
        System.out.println("  worstFitness = " + worstFitness);
        for(int i=0; i<population.size(); i++){
            System.out.println("      " + i + "\t, fitness=" + population.get(i).fitness);
        }
    }
    
    int getBest(){
        int res = 0;
        for(int i=0; i<population.size(); i++){
            if(population.get(res).fitness > population.get(i).fitness){
                res = i;
            }
        }
        return res;
    }
}
