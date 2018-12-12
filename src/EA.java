
import java.util.ArrayList;
import java.util.Collections;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Generates "reasonable" (feasible, if possible) solutions to the final_EA run.
 * @author Jiri
 */
public class EA {
    ArrayList<Individual> population;
    int popSize;
    
    public EA(int n){
        popSize = n;
        population = new ArrayList<>(n);
        for(int i=0; i<n; i++){
            Individual cand = new Individual(i, (short)(ReadData.nbZones), ReadData.startZone, ReadData.startAirport);
            cand.constructTrip();
            population.add(cand);
        }
    }
    
    public Individual run(long initialTimeStamp, double timeLeft) throws Exception{
        int gen = 0;
        int tourn1 = 3;
        int tourn2 = 3;
        double pCross = 0.75;
        double p1 = 0.5;
        int nMut = 10;
        
        ArrayList<Individual> saPop = new ArrayList<>();
        while(true){
            long startTime = System.currentTimeMillis();
            //---
            ArrayList<Individual> tempPop = new ArrayList<>();
            while(tempPop.size() < population.size()){
                Individual par1 = population.get(tournament(tourn1)).copyConstructor();
                Individual par2 = population.get(tournament(tourn2)).copyConstructor();
                Individual child = null; 
                if(Main.rnd.nextDouble() < pCross){
                    if(Main.rnd.nextDouble() < p1){
                        child = par1;
                        child.crossover2point(par2);
                    }
                    else{
                        child = par2;
                        child.crossover2point(par1);
                    }
                }
                else{
                    if(Main.rnd.nextDouble() < p1){
                        child = par1;
                    }
                    else{
                        child = par2;
                    }
                    child.mutate_swap(nMut);
                }
                child.constructTrip();
                tempPop.add(child);
            }
            tempPop.addAll(population);
            Collections.sort(tempPop, Individual.compByFitness);
            population.clear();
            population.add(tempPop.get(0));
            int i = 1;
            //--- take unique solutions
            while(i < tempPop.size() && population.size() < popSize){
                if(!tempPop.get(i).equals(tempPop.get(i-1))){
                    population.add(tempPop.get(i));
                }
                i++;
            }
            //--- fill in with solutions' copies
            while(population.size() < popSize){
                population.add(tempPop.get(Main.rnd.nextInt(tempPop.size())));
            }
            //--- choose individuals to saPop
            for(i=0; i<population.size() && saPop.size()<Main.poolSizeFinalEA; i++){
                if(population.get(i).collisions.isEmpty() && (!saPop.contains(population.get(i)))){
                    saPop.add(population.get(i).copyConstructor());
                    Individual newInd = new Individual(i, (short)(ReadData.nbZones), ReadData.startZone, ReadData.startAirport);
                    newInd.constructTrip();
                    population.set(i, newInd);
                }
            }
            //--- check time left
            long endTime = System.currentTimeMillis();
            long oneGenTime = endTime - startTime;
            if(timeLeft - (endTime - initialTimeStamp)/1000.0 < 1.5*oneGenTime/1000.0){
                break;
            }
            if((saPop.size() >= Main.poolSizeFinalEA) || (gen >= Main.maxGen) || ((gen > Main.minGen) && population.get(0).collisions.isEmpty())){
                System.out.println("hop: " + gen);
                break;
            }
            gen++;
        }
        Collections.sort(population, Individual.compByFitness);
        Individual best = getBest();

        //--- EA_final
        System.out.println("EA_final pool size = " + saPop.size());
        Collections.sort(saPop, Individual.compByFitness);
        while(saPop.size() > Main.popSizeFinalEA){
            saPop.remove(saPop.size()-1);
        }
        System.out.println("EA_final population size = " + saPop.size());
        EA_final eaFinal = new EA_final(saPop);
        best = eaFinal.run(Main.maxFinalEAGenerations);
        return best;
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
    
    public Individual getBest(){
        Individual res = population.get(0);
        for(int i=1; i<population.size(); i++){
            if(res.fitness > population.get(i).fitness){
                res = population.get(i);
            }
        }
        return res;
    }
}
