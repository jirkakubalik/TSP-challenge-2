import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jiri
 */
public class Individual {
    public int seed;
    short[] priorityList;
    short[] tripZones;      //--- absolute indexes
    short[] tripAirports;   //--- absolute indexes; first airport is set to the startAirportName
    ArrayList<Short> collisions; //--- starting points where no airport can be found from previous destination
    short startZone;    //--- absolute index
    short startAirport; //--- absolute index
    int fitness;
    boolean valid = true;
    static short candSize = 50;
    static boolean toShuffle = false;
    //---
    int nonImprovingMoves;
    int lastImprovement;
    int bestEver = Integer.MAX_VALUE;
    short[] bestEverTripAirports;    
    
    public Individual(){}
    
    public Individual(int seed, short days, short startZone, short startAirport) {
        this.seed = seed;
        this.startZone = startZone;
        this.startAirport = startAirport;
        //---
        tripZones = new short[days+1];
        tripZones[0] = this.startZone;
        tripZones[days] = this.startZone;
        for(short i=1; i<days; i++){
            tripZones[i] = -1;
        }
        //---
        tripAirports = new short[days+1];
        tripAirports[0] = this.startAirport;
        for(short i=1; i<days+1; i++){
            tripAirports[i] = -1;
        }
//        createPriorityListRandom(days);
        createPriorityListGreedy(days);
        collisions = new ArrayList<>();
    }
    
    void createPriorityListRandom(short days){
        priorityList = new short[days-1];   //--- the first dayId za is given
        for(short i=0; i<days-1; i++){
            priorityList[i] = (short)(i+1);
        }
        //--- shuffle priorityList
        for(short i=0; i<days-1; i++){
            short cand = (short)Main.rnd.nextInt(days-1);
            short temp = priorityList[i];
            priorityList[i] = priorityList[cand];
            priorityList[cand] = temp;
        }
    }

    void createPriorityListGreedy(short days){
//        short nbDesert = 40;
        ArrayList<Short> toUse = new ArrayList<>();
        for(short j=0; j<days-1; j++){
            toUse.add((short)(j+1));
        }
        ArrayList<Short> available = new ArrayList<>();
        priorityList = new short[days-1];   //--- the first dayId za is given
        int i = 0;
        for(short j=0; j<Main.nbDesert; j++){
            priorityList[i] = toUse.remove(Main.rnd.nextInt(toUse.size()));
            available.remove((Short)priorityList[i]);
//System.out.print(" " + priorityList[day2]);
            if(priorityList[i] > 1 && toUse.contains((Short)((short)(priorityList[i]-1))) && !available.contains((Short)((short)(priorityList[i]-1)))){
                available.add((short)(priorityList[i]-1));
            }
            if(priorityList[i] < days-1 && toUse.contains((Short)((short)(priorityList[i]+1))) && !available.contains((Short)((short)(priorityList[i]+1)))){
                available.add((short)(priorityList[i]+1));
            }
            i++;
        }
        while(!toUse.isEmpty()){
            priorityList[i] = available.remove(Main.rnd.nextInt(available.size()));
            toUse.remove((Short)priorityList[i]);
//System.out.print(" " + priorityList[day2]);
            if(priorityList[i] > 1 && toUse.contains((Short)((short)(priorityList[i]-1))) && !available.contains((Short)((short)(priorityList[i]-1)))){
                available.add((short)(priorityList[i]-1));
            }
            if(priorityList[i] < days-1 && toUse.contains((Short)((short)(priorityList[i]+1))) && !available.contains((Short)((short)(priorityList[i]+1)))){
                available.add((short)(priorityList[i]+1));
            }
            i++;
        }
    }

    boolean checkPriorityList(short days){
        ArrayList<Short> toUse = new ArrayList<>();
        for(short j=0; j<days-1; j++){
            toUse.add((short)(j+1));
        }
        for(short j=0; j<priorityList.length; j++){
            toUse.remove((Short)priorityList[j]);
        }
        if(toUse.isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }
    
    public Individual copyConstructor(){
        Individual newInd = new Individual();
        newInd.seed = this.seed;
        newInd.startZone = this.startZone;
        newInd.startAirport = this.startAirport;
        newInd.fitness = this.fitness;
        newInd.valid = this.valid;
        
        //---
        newInd.tripZones = Arrays.copyOf(this.tripZones, this.tripZones.length);
        newInd.tripAirports = Arrays.copyOf(this.tripAirports, this.tripAirports.length);
        newInd.priorityList = Arrays.copyOf(this.priorityList, this.priorityList.length);
        newInd.collisions = new ArrayList<>();
        for(short i=0; i<this.collisions.size(); i++){
            newInd.collisions.add(this.collisions.get(i));
        }
        return newInd;
    }

    //--- Comparator for sorting Individuals by fitness
    public static Comparator<Individual> compByFitness = new Comparator<Individual>() {
	public int compare(Individual ind1, Individual ind2) {
           if(ind2.fitness < ind1.fitness)
               return 1;
           else if(ind1.fitness < ind2.fitness)
               return -1;
           else
               return 0;
        }
    };

    public boolean isEqualTo(Individual other){
        if(this.fitness != other.fitness){
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object other){
        if (other == this) return true;
        if (other == null || !(other instanceof Individual))return false;
        Individual otherInd = (Individual)other;
        if(this.fitness != otherInd.fitness){
            return false;
        }
        return true;
    }

    /**
     * EA operator.
     * Operates on priorityList.
     * @param mate 
     */
    public void crossoverUniform(Individual mate){
        double p1 = 0.5;

        //--- initialization
        ArrayList<Short> toUse = new ArrayList<>();
        for(int i=0; i<this.priorityList.length; i++){
            if(Main.rnd.nextDouble() < p1){
                toUse.add(this.priorityList[i]);
                this.priorityList[i] = -1;
            }
        }
        //--- inherit from mate
        for(int i=0; i<this.priorityList.length; i++){
            if(this.priorityList[i] == -1){
                if(toUse.contains(mate.priorityList[i])){
                    this.priorityList[i] = mate.priorityList[i];
                    toUse.remove(new Short(this.priorityList[i]));
                }
            }
        }
        //--- fill in the remaining zones
        for(int i=0; i<this.priorityList.length; i++){
            if(this.priorityList[i] == -1){
                this.priorityList[i] = toUse.remove(Main.rnd.nextInt(toUse.size()));
            }
        }
    }

    /**
     * EA operator.
     * Operates on priorityList.
     */
    public void crossover2point(Individual mate){
        short lp, rp, temp;

        lp = (short)(1 + Main.rnd.nextInt(this.priorityList.length-1));
        rp = (short)(1 + Main.rnd.nextInt(this.priorityList.length-1));
        if(lp > rp){
            temp = lp;
            lp = rp; 
            rp = temp;
        }
        
        //--- initialization
        ArrayList<Short> toUse = new ArrayList<>();
        if(Main.rnd.nextBoolean()){
            for(int i=0; i<lp; i++){
                toUse.add(this.priorityList[i]);
                this.priorityList[i] = -1;
            }
            for(int i=rp; i<this.priorityList.length; i++){
                toUse.add(this.priorityList[i]);
                this.priorityList[i] = -1;
            }
        }
        else{
            for(int i=lp; i<=rp; i++){
                toUse.add(this.priorityList[i]);
                this.priorityList[i] = -1;
            }
        }
        //--- inherit from mate
        for(int i=0; i<this.priorityList.length; i++){
            if(this.priorityList[i] == -1){
                if(toUse.contains(mate.priorityList[i])){
                    this.priorityList[i] = mate.priorityList[i];
                    toUse.remove(new Short(this.priorityList[i]));
                }
            }
        }
        //--- fill in the remaining zones
        for(int i=0; i<this.priorityList.length; i++){
            if(this.priorityList[i] == -1){
                this.priorityList[i] = toUse.remove(Main.rnd.nextInt(toUse.size()));
            }
        }
    }
    
    /**
     * EA operator.
     * Operates on priorityList.
     * Swaps a collision dayId with a random one.
     */
    public void mutate_swap(int n){
        short k = 0;
        n = 1 + Main.rnd.nextInt(n);
        while(k < n){
            //--- find a collision dayId
            short coll;
            if(!collisions.isEmpty()){
                coll = collisions.get(Main.rnd.nextInt(collisions.size()));
            }
            else{
                coll = (short)Main.rnd.nextInt(priorityList.length);
            }
            short j;
            if(coll > 0){
                j = (short)(Main.rnd.nextInt(coll));
            }
            else{
                j = (short)Main.rnd.nextInt(priorityList.length);
            }
            //--- swap
            short temp = priorityList[coll];
            priorityList[coll] = priorityList[j];
            priorityList[j] = temp;
            k++;
        }
    }

    /**
     * Shifts all zones by the specified nb of positions.
     * @param by 
     */
    public void shiftTripZones(int by){
        short[] newTripZones = new short[tripZones.length];
        newTripZones[0] = tripZones[0];
        newTripZones[tripZones.length-1] = tripZones[tripZones.length-1];
        for(int i=0; i<tripZones.length-2; i++){
            newTripZones[(i+by)%(tripZones.length-2)+1] = tripZones[i+1];
        }
        tripZones = newTripZones;
    }
    
    /**
     * EA_final operator.
     * Operates on tripZones.
     * @param mate 
     */
    public void eaFinal_crossoverUniform(Individual mate){
        double p1 = 0.5;
        double pS = 0.0;

        if(Main.rnd.nextDouble() < pS){
            shiftTripZones(1 + Main.rnd.nextInt(tripZones.length-3));
        }
        if(Main.rnd.nextDouble() < pS){
            mate.shiftTripZones(1 + Main.rnd.nextInt(mate.tripZones.length-3));
        }

        //--- initialization
        ArrayList<Short> toUse = new ArrayList<>();
        for(int i=1; i<this.tripZones.length-1; i++){
            if(Main.rnd.nextDouble() < p1){
                toUse.add(this.tripZones[i]);
                this.tripZones[i] = -1;
            }
        }
        //--- inherit from mate
        for(int i=1; i<this.tripZones.length-1; i++){
            if(this.tripZones[i] == -1){
                if(toUse.contains(mate.tripZones[i])){
                    this.tripZones[i] = mate.tripZones[i];
                    toUse.remove(new Short(this.tripZones[i]));
                }
            }
        }
        //--- fill in the remaining zones
        for(int i=1; i<this.tripZones.length-1; i++){
            if(this.tripZones[i] == -1){
                this.tripZones[i] = toUse.remove(Main.rnd.nextInt(toUse.size()));
            }
        }
    }

    /**
     * EA_final operator.
     * Operates on tripZones.
     * @param mate 
     */
    public void eaFinal_crossover2point(Individual mate){
        short lp, rp, temp;
        double pS = 0.0;

        if(Main.rnd.nextDouble() < pS){
            shiftTripZones(1 + Main.rnd.nextInt(tripZones.length-3));
        }
        if(Main.rnd.nextDouble() < pS){
            mate.shiftTripZones(1 + Main.rnd.nextInt(mate.tripZones.length-3));
        }

        lp = (short)(1 + Main.rnd.nextInt(this.tripZones.length-2));
        rp = (short)(1 + Main.rnd.nextInt(this.tripZones.length-2));
        if(lp > rp){
            temp = lp;
            lp = rp; 
            rp = temp;
        }
        
        //--- initialization
        ArrayList<Short> toUse = new ArrayList<>();
        if(Main.rnd.nextBoolean()){
            for(int i=1; i<lp; i++){
                toUse.add(this.tripZones[i]);
                this.tripZones[i] = -1;
            }
            for(int i=rp; i<this.tripZones.length-1; i++){
                toUse.add(this.tripZones[i]);
                this.tripZones[i] = -1;
            }
        }
        else{
            for(int i=lp; i<=rp; i++){
                toUse.add(this.tripZones[i]);
                this.tripZones[i] = -1;
            }
        }
        //--- inherit from mate
        for(int i=1; i<this.tripZones.length-1; i++){
            if(this.tripZones[i] == -1){
                if(toUse.contains(mate.tripZones[i])){
                    this.tripZones[i] = mate.tripZones[i];
                    toUse.remove(new Short(this.tripZones[i]));
                }
            }
        }
        //--- fill in the remaining zones
        for(int i=1; i<this.tripZones.length-1; i++){
            if(this.tripZones[i] == -1){
                this.tripZones[i] = toUse.remove(Main.rnd.nextInt(toUse.size()));
            }
        }
    }
    
    /**
     * EA_final operator.
     * Operates on tripZones.
     * @param n 
     */
    public void eaFinal_mutate_swap(int n){
        for(int k=0; k<n; k++){
            short i = (short)(1 + Main.rnd.nextInt(this.tripZones.length-2));
            short j = (short)(1 + Main.rnd.nextInt(this.tripZones.length-2));
            short temp = this.tripZones[i];
            this.tripZones[i] = this.tripZones[j];
            this.tripZones[j] = temp;
        }
    }

    /**
     * EA_final operator.
     * Operates on tripZones.
     * Inverts a randomly chosen section within the tripZones.
     */
    public void eaFinal_mutate_invert(){
        short lp = (short)(1 + Main.rnd.nextInt(this.tripZones.length-2));
        short rp = (short)(1 + Main.rnd.nextInt(this.tripZones.length-2));
        if(lp > rp){
            short temp = lp;
            lp = rp;
            rp = temp;
        }
        short[] seq = new short[rp-lp+1];
        short k = 0;
        for(int i=rp; i>=lp; i--){
            seq[k++] = this.tripZones[i];
        }
        k = 0;  //--- tohle jde lip
        for(int i=lp; i<=rp; i++){
            this.tripZones[i] = seq[k++];
        }
    }

    /**
     * EA_final operator.
     * Operates on tripZones.
     */
    public void eaFinal_mutate(){
        switch(Main.rnd.nextInt(2)){
            case 0: eaFinal_mutate_invert(); break;
            case 1: eaFinal_mutate_swap(1 + Main.rnd.nextInt(5)); break;
        }
    }
    
    /**
     * EA_final operator.
     * Input: tripZones
     * @param getTrip
     * @return 
     */
    public int calculateTripPrice(boolean getTrip){
        int res = 0;
        boolean ok = true;
        collisions.clear();
        boolean valid = true;
        HashMap<Short, Integer> stops = new HashMap<>();   //--- to-cost, absolute ap index
        HashMap<Short, Short> hops = new HashMap<>();   //--- to-from, absolute ap indexes
        
        //--- clear tripAirports info
        if(getTrip){
            tripAirports[0] = startAirport;
            for(short i=1; i<tripAirports.length; i++){
                tripAirports[i] = -1;
            }
        }

        //--- traverse a tour by zone tuples from-to 
        short toZone; //--- index of the destination zone
        HashMap<Short,Integer> startingAirports = new HashMap<Short,Integer>();    //--- fromAirports, <airport, price_to_get_there>
        HashMap<Short,Integer> reachableAirports = new HashMap<Short,Integer>();    //--- toAirports, <airport, price_to_get_there>
        startingAirports.put(startAirport, 0);
        short day = 0;
        while(day < tripZones.length-1){
            reachableAirports = new HashMap<Short,Integer>();
            toZone = tripZones[day + 1];
            //--- for all tuples <fromAirport-toAirport>
            for(Map.Entry<Short,Integer> ac: startingAirports.entrySet()){
                Short fromAirport = ac.getKey();
                int fromAirportCost = ac.getValue();
                Short costFromTo;
                //---
                for(Short toAirport: ReadData.zoneAirports[toZone]){
                    //--- check flights fromAirport-toAirport
                    costFromTo = ReadData.flights[day][fromAirport][toAirport];
                    if(costFromTo != ReadData.noFlightCost){
                        int newCost = fromAirportCost + costFromTo;
                        if(reachableAirports.get(toAirport) == null || newCost < reachableAirports.get(toAirport)){
                            reachableAirports.put(toAirport, newCost);
                            if(getTrip){
                                stops.put((Short)toAirport, (Integer)newCost);
                                hops.put((Short)toAirport, (Short)fromAirport);
                            }
                        }
                    }
                }                    
            }
            //--- if no reachable triplet exists
            if(reachableAirports.isEmpty()){
                valid = false;
                if(ok){
                    if(day+1 < tripZones.length-1){
                        collisions.add(new Short((short)(day+1)));  //--- here the problem originates
                    }
                    else{
                        collisions.add(new Short((short)(day)));  //--- here the problem originates
                    }
                }
                res += ReadData.noFlightCost;
                startingAirports = new HashMap<Short,Integer>();    //--- fromAirports, <airport, price_to_get_there>
                for(Short ap: ReadData.zoneAirports[toZone]){
                    startingAirports.put(ap, res);
                }
                ok = false;
            }
            else{
                //--- find minimum cost tripZones
                int minCost = Integer.MAX_VALUE;
                for(Map.Entry<Short,Integer> ac: reachableAirports.entrySet()){
                    if(minCost > ac.getValue()){
                        minCost = ac.getValue();
                    }
                }
                res = minCost;
                //--- take all raechable entries
                startingAirports = new HashMap<Short,Integer>();
                for(Map.Entry<Short,Integer> ac: reachableAirports.entrySet()){
                    startingAirports.put(ac.getKey(), ac.getValue());
                }
                ok = true;
            }
            day++;
        }
        //---
        if(true){
//        if(getTrip && valid){
            //--- find the cheapest way to destination zone
            tripAirports[0] = ReadData.startAirport;
            short pos = (short)(tripAirports.length-1);
            Short arrivalAp = null;
            while(pos > 0){
                if(arrivalAp == null){
                    int bestPrice = Integer.MAX_VALUE;
                    for(Short ap: ReadData.zoneAirports[tripZones[pos]]){
                        if(stops.get(ap) != null && stops.get(ap) < bestPrice){
                            bestPrice = stops.get(ap);
                            arrivalAp = ap;
                        }
                    }
                }
                if(arrivalAp != null){
                    tripAirports[pos] = arrivalAp;
                    arrivalAp = hops.get(arrivalAp);
                }
                else{
                    short z = tripZones[pos];
                    tripAirports[pos] = ReadData.zoneAirports[z][Main.rnd.nextInt(ReadData.zoneAirports[z].length)];
                }
                pos--;
            }
        }

        //---
        this.fitness = res;
        return res;
    }
    
    short chooseDayByDistToPred(int t){
        short res = (short)(1 + Main.rnd.nextInt(tripAirports.length-2));
        short i = 1;
        while(i < t){
            short cand = (short)(1 + Main.rnd.nextInt(tripAirports.length-2));
            if(ReadData.flights[res-1][tripAirports[res-1]][tripAirports[res]] < ReadData.flights[cand-1][tripAirports[cand-1]][tripAirports[cand]]){
                res = cand;
            }
            i++;
        }
        return res;
    }
    
    public int lsOpSwap(int id){
        short t = 2;
        int diff = 0;
        int[] distToNeighbors = new int[tripZones.length];
        for(short i=1; i<tripZones.length-1; i++){
            distToNeighbors[i] = ReadData.flights[i-1][tripAirports[i-1]][tripAirports[i]] + ReadData.flights[i][tripAirports[i]][tripAirports[i+1]];
        }
        //--- choose a candidate day with large distToNeighbors
        short cand = chooseDayByDistToPred(t);
        //--- find a possible replacement
        short k = (short)(Main.rnd.nextInt(tripZones.length-2));
        for(int j=0; j<tripZones.length/3; j++){
            short i = (short)(k + 1);
            short newCandIn = ReadData.flights[i-1][tripAirports[i-1]][tripAirports[cand]];
            short newCandOut = ReadData.flights[i][tripAirports[cand]][tripAirports[i+1]];
            short newRepIn = ReadData.flights[cand-1][tripAirports[cand-1]][tripAirports[i]];
            short newRepOut = ReadData.flights[cand][tripAirports[i]][tripAirports[cand+1]];
            diff = (distToNeighbors[cand] + distToNeighbors[i]) - (newCandIn + newCandOut + newRepIn + newRepOut);
            if(diff > 0){
                short temp = tripZones[cand];
                tripZones[cand] = tripZones[i];
                tripZones[i] = temp;
                //---
                temp = tripAirports[cand];
                tripAirports[cand] = tripAirports[i];
                tripAirports[i] = temp;
                //---
                System.out.print("  " + id + ".\tfinetuning: " + fitness + " -> " + (fitness-diff) + "\n");
                fitness -= diff;
                break;
            }
            else{
                diff = 0;
            }
            k = (short)((k+1) % (tripZones.length-2));
        }
        return diff;
    }

    public int saOpSwap(int iter, int id, int worstFitness, int bestFitness){
        short t = 2;
        int diff = 0;
        int bestDiff = -1000000;
        short bestRepc = -1;
        double pAcc = 0.05;
        int[] distToNeighbors = new int[tripZones.length];
        for(short i=1; i<tripZones.length-1; i++){
            distToNeighbors[i] = ReadData.flights[i-1][tripAirports[i-1]][tripAirports[i]] + ReadData.flights[i][tripAirports[i]][tripAirports[i+1]];
        }
        //--- choose a candidate day with large distToNeighbors
        short cand = chooseDayByDistToPred(t);
        //--- find a possible replacement
        int stepBack = 0;
        if(fitness > bestFitness){
            stepBack = fitness - worstFitness;
        }
        boolean done = false;
        short k = (short)(Main.rnd.nextInt(tripZones.length-2));
        for(int j=0; j<tripZones.length/3 && !done; j++, k = (short)((k+1) % (tripZones.length-2))){
            short repc = (short)(k + 1);
            if(cand == repc){
                continue;
            }
            short newCandIn = ReadData.flights[repc-1][tripAirports[repc-1]][tripAirports[cand]];
            short newCandOut = ReadData.flights[repc][tripAirports[cand]][tripAirports[repc+1]];
            short newRepIn = ReadData.flights[cand-1][tripAirports[cand-1]][tripAirports[repc]];
            short newRepOut = ReadData.flights[cand][tripAirports[repc]][tripAirports[cand+1]];
            diff = (distToNeighbors[cand] + distToNeighbors[repc]) - (newCandIn + newCandOut + newRepIn + newRepOut);
            if((diff > 0) || (diff > stepBack && Main.rnd.nextDouble() < pAcc)){
                if(bestDiff < diff){
                    bestDiff = diff;
                    bestRepc = repc;
                    if(diff > 0){
                        done = true;
                        break;
                    }
                }
            }
        }
        if(bestRepc != -1){
            short temp = tripZones[cand];
            tripZones[cand] = tripZones[bestRepc];
            tripZones[bestRepc] = temp;
            //---
            temp = tripAirports[cand];
            tripAirports[cand] = tripAirports[bestRepc];
            tripAirports[bestRepc] = temp;
            //---
//            System.out.print("  " + iter + ".  " + id + "\topSwap: " + fitness + " -> " + (fitness-bestDiff) + "\n");
            fitness -= bestDiff;
            nonImprovingMoves = 0;
        }
        //--- Is it an improving move?
        if(bestDiff > 0){
            nonImprovingMoves = 0;
        }
        else{
            nonImprovingMoves++;
        }

        return bestDiff;
    }

    public int saOpSwapFull(int iter, int id, int worstFitness, int bestFitness){
        short t = 2;
        int diff = 0;
        int bestDiff = -1000000;
        short bestDay1 = -1, bestDay2 = -1;
        short apDay1 = -1, apDay2 = -1;
        short bestApDay1 = -1, bestApDay2 = -1;
        double pAcc = 0.05;
        int[] distToNeighbors = new int[tripZones.length];
        for(short i=1; i<tripZones.length-1; i++){
            distToNeighbors[i] = ReadData.flights[i-1][tripAirports[i-1]][tripAirports[i]] + ReadData.flights[i][tripAirports[i]][tripAirports[i+1]];
        }
        int stepBack = 0;
        if(fitness > bestFitness){
            stepBack = fitness - worstFitness;
        }
        boolean done = false;

        //--- choose a candidate day with large distToNeighbors
        short day1 = chooseDayByDistToPred(t);
        for(int x=0; x<ReadData.zoneAirports[tripZones[day1]].length; x++){
            apDay1 = ReadData.zoneAirports[tripZones[day1]][x];
            //--- find a possible replacement
            short k = (short)(Main.rnd.nextInt(tripZones.length-2));
            for(int j=0; j<tripZones.length/3 && !done; j++, k = (short)((k+1) % (tripZones.length-2))){
                short day2 = (short)(k + 1);
                if(Math.abs(day1-day2) < 2){
                    continue;
                }
                for(int y=0; y<ReadData.zoneAirports[tripZones[day2]].length; y++){
                    apDay2 = ReadData.zoneAirports[tripZones[day2]][y];
                    short newDay1In = ReadData.flights[day1-1][tripAirports[day1-1]][apDay2];
                    short newDay1Out = ReadData.flights[day1][apDay2][tripAirports[day1+1]];
                    short newDay2In = ReadData.flights[day2-1][tripAirports[day2-1]][apDay1];
                    short newDay2Out = ReadData.flights[day2][apDay1][tripAirports[day2+1]];
                    boolean ok = false;
                    if(newDay1In != ReadData.noFlightCost 
                       && newDay1Out != ReadData.noFlightCost
                       && newDay2In != ReadData.noFlightCost
                       && newDay2Out != ReadData.noFlightCost){
                        ok = true;
                    }
                    if(!ok){
                        continue;
                    }
                    diff = (distToNeighbors[day1] + distToNeighbors[day2]) - (newDay1In + newDay1Out + newDay2In + newDay2Out);
                    if((diff > 0) || (diff > stepBack && Main.rnd.nextDouble() < pAcc)){
                        if(bestDiff < diff){
                            bestDiff = diff;
                            bestDay1 = day1;
                            bestDay2 = day2;
                            bestApDay1 = apDay1;
                            bestApDay2 = apDay2;
                            if(diff > 0){
                                done = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(bestDay1 != -1){
            short temp = tripZones[bestDay1];
            tripZones[bestDay1] = tripZones[bestDay2];
            tripZones[bestDay2] = temp;
            //---
            tripAirports[bestDay1] = bestApDay2;
            tripAirports[bestDay2] = bestApDay1;
            //---
//            System.out.print("  " + iter + ".  " + id + "\topSwapFull: " + fitness + " -> " + (fitness-bestDiff) + "\n");
            fitness -= bestDiff;
            nonImprovingMoves = 0;
        }
        //--- Is it an improving move?
        if(bestDiff > 0){
            nonImprovingMoves = 0;
        }
        else{
            nonImprovingMoves++;
        }

        return bestDiff;
    }
    
    public int saOpTripleSwap(int iter, int id, int worstFitness, int bestFitness){
        short t = 2;
        int diff = 0;
        int bestDiff = -1000000;
        short bestDay1 = -1, bestDay2 = -1, bestDay3 = -1;
        double pAcc = 0.05;
        int[] distToNeighbors = new int[tripZones.length];
        for(short i=1; i<tripZones.length-1; i++){
            distToNeighbors[i] = ReadData.flights[i-1][tripAirports[i-1]][tripAirports[i]] + ReadData.flights[i][tripAirports[i]][tripAirports[i+1]];
        }
        int stepBack = 0;
        if(fitness > bestFitness){
            stepBack = fitness - worstFitness;
        }
        boolean done = false;
        //--- (1) choose a candidate day1 with large distToPrev
        short day1 = chooseDayByDistToPred(5);
        //--- (2) choose day2 so that any ap from day1 fits to day2
        short k = (short)(Main.rnd.nextInt(tripZones.length-2));    //--- start from a random day
        for(int j=0; j<tripZones.length/3 && !done; j++, k = (short)((k+1) % (tripZones.length-2))){
            short day2 = (short)(k + 1);
            if((ReadData.flights[day2-1][tripAirports[day2-1]][tripAirports[day1]] == ReadData.noFlightCost)
               || (ReadData.flights[day2][tripAirports[day1]][tripAirports[day2+1]] == ReadData.noFlightCost) 
               || Math.abs(day1-day2)<2){
                continue;
            }
            //--- (3) choose day3 so that its ap fits to day1 
            //---     and ap from day2 fits to day3
            short m = (short)(Main.rnd.nextInt(tripZones.length-2));    //--- start from a random day
            for(int l=0; l<tripZones.length/3 && !done; l++, m = (short)((m+1) % (tripZones.length-2))){
                short day3 = (short)(m + 1);
                if((ReadData.flights[day1-1][tripAirports[day1-1]][tripAirports[day3]] == ReadData.noFlightCost)    //--- ap3 fits in day1
                   || (ReadData.flights[day1][tripAirports[day3]][tripAirports[day1+1]] == ReadData.noFlightCost)
                   || (ReadData.flights[day3-1][tripAirports[day3-1]][tripAirports[day2]] == ReadData.noFlightCost) //--- ap2 fits in day3
                   || (ReadData.flights[day3][tripAirports[day2]][tripAirports[day3+1]] == ReadData.noFlightCost)
                   || Math.abs(day1-day3)<2
                   || Math.abs(day2-day3)<2){
                    continue;
                }
                //--- check move quality
                short newInDay1 = ReadData.flights[day1-1][tripAirports[day1-1]][tripAirports[day3]];
                short newOutDay1 = ReadData.flights[day1][tripAirports[day3]][tripAirports[day1+1]];
                short newInDay2 = ReadData.flights[day2-1][tripAirports[day2-1]][tripAirports[day1]];
                short newOutDay2 = ReadData.flights[day2][tripAirports[day1]][tripAirports[day2+1]];
                short newInDay3 = ReadData.flights[day3-1][tripAirports[day3-1]][tripAirports[day2]];
                short newOutDay3 = ReadData.flights[day3][tripAirports[day2]][tripAirports[day3+1]];
                diff = (distToNeighbors[day1] + distToNeighbors[day2] + distToNeighbors[day3]) - (newInDay1 + newOutDay1 + newInDay2 + newOutDay2 + newInDay3 + newOutDay3);
                if((diff > 0) || (diff > stepBack && Main.rnd.nextDouble() < pAcc)){
                    if(bestDiff < diff){
                        bestDiff = diff;
                        bestDay1 = day1;
                        bestDay2 = day2;
                        bestDay3 = day3;
                        if(diff > 0){
                            done = true;
                            break;
                        }
                    }
                }
            }
        }
        if(bestDay1 != -1){
            short temp = tripZones[bestDay3];
            tripZones[bestDay3] = tripZones[bestDay2];
            tripZones[bestDay2] = tripZones[bestDay1];
            tripZones[bestDay1] = temp;
            //---
            temp = tripAirports[bestDay3];
            tripAirports[bestDay3] = tripAirports[bestDay2];
            tripAirports[bestDay2] = tripAirports[bestDay1];
            tripAirports[bestDay1] = temp;
            //---
//            System.out.print("  " + iter + ".  " + id + "\ttripleSwap: " + fitness + " -> " + (fitness-bestDiff) + "\n");
            fitness -= bestDiff;
        }
        //--- Is it an improving move?
        if(bestDiff > 0){
            nonImprovingMoves = 0;
        }
        else{
            nonImprovingMoves++;
        }

        return bestDiff;
    }

    public int saOpTripleSwapFull(int iter, int id, int worstFitness, int bestFitness){
        short t = 2;
        int diff = 0;
        int bestDiff = -1000000;
        short bestDay1 = -1, bestDay2 = -1, bestDay3 = -1;
        short apDay1 = -1, apDay2 = -1, apDay3 = -1;
        short bestApDay1 = -1, bestApDay2 = -1, bestApDay3 = -1;
        double pAcc = 0.05;
        int[] distToNeighbors = new int[tripZones.length];
        for(short i=1; i<tripZones.length-1; i++){
            distToNeighbors[i] = ReadData.flights[i-1][tripAirports[i-1]][tripAirports[i]] + ReadData.flights[i][tripAirports[i]][tripAirports[i+1]];
        }
        int stepBack = 0;
        if(fitness > bestFitness){
            stepBack = fitness - worstFitness;
        }
        boolean done = false;
        
        //--- (1) choose a candidate day1 with large distToPrev
        short day1 = chooseDayByDistToPred(5);
        for(int x=0; x<ReadData.zoneAirports[tripZones[day1]].length; x++){
            apDay1 = ReadData.zoneAirports[tripZones[day1]][x];
        
            //--- (2) choose day2 so that any ap from day1 fits to day2
            short k = (short)(Main.rnd.nextInt(tripZones.length-2));    //--- start from a random day
            for(int j=0; j<tripZones.length/3 && !done; j++, k = (short)((k+1) % (tripZones.length-2))){
                short day2 = (short)(k + 1);
                boolean ok = false;
                if((ReadData.flights[day2-1][tripAirports[day2-1]][apDay1] != ReadData.noFlightCost)
                   && (ReadData.flights[day2][apDay1][tripAirports[day2+1]] != ReadData.noFlightCost) 
                   && Math.abs(day1-day2)>1){
                    ok = true;
                }
                if(!ok){
                    continue;
                }
                for(int y=0; y<ReadData.zoneAirports[tripZones[day2]].length; y++){
                    apDay2 = ReadData.zoneAirports[tripZones[day2]][y];

                    //--- (3) choose day3 so that its ap fits to day1 
                    //---     and ap from day2 fits to day3
                    short m = (short)(Main.rnd.nextInt(tripZones.length-2));    //--- start from a random day
                    for(int l=0; l<tripZones.length/3 && !done; l++, m = (short)((m+1) % (tripZones.length-2))){
                        short day3 = (short)(m + 1);
                        for(int z=0; z<ReadData.zoneAirports[tripZones[day3]].length; z++){
                            apDay3 = ReadData.zoneAirports[tripZones[day3]][z];
                            ok = false;
                            if((ReadData.flights[day1-1][tripAirports[day1-1]][apDay3] != ReadData.noFlightCost)    //--- ap3 fits in day1
                               && (ReadData.flights[day1][apDay3][tripAirports[day1+1]] != ReadData.noFlightCost)
                               && (ReadData.flights[day3-1][tripAirports[day3-1]][apDay2] != ReadData.noFlightCost) //--- ap2 fits in day3
                               && (ReadData.flights[day3][apDay2][tripAirports[day3+1]] != ReadData.noFlightCost)
                               && Math.abs(day1-day3)>1
                               && Math.abs(day2-day3)>1){
                                ok = true;
                            }
                            if(!ok){
                                continue;
                            }
                            //--- check move quality
                            short newInDay1 = ReadData.flights[day1-1][tripAirports[day1-1]][apDay3];   //--- apDay3 in day1
                            short newOutDay1 = ReadData.flights[day1][apDay3][tripAirports[day1+1]];
                            short newInDay2 = ReadData.flights[day2-1][tripAirports[day2-1]][apDay1];   //--- apDay1 in day2
                            short newOutDay2 = ReadData.flights[day2][apDay1][tripAirports[day2+1]];
                            short newInDay3 = ReadData.flights[day3-1][tripAirports[day3-1]][apDay2];   //--- apDay2 in day3
                            short newOutDay3 = ReadData.flights[day3][apDay2][tripAirports[day3+1]];
                            diff = (distToNeighbors[day1] + distToNeighbors[day2] + distToNeighbors[day3]) - (newInDay1 + newOutDay1 + newInDay2 + newOutDay2 + newInDay3 + newOutDay3);
                            if((diff > 0) || (diff > stepBack && Main.rnd.nextDouble() < pAcc)){
                                if(bestDiff < diff){
                                    bestDiff = diff;
                                    bestDay1 = day1;
                                    bestDay2 = day2;
                                    bestDay3 = day3;
                                    bestApDay1 = apDay1;
                                    bestApDay2 = apDay2;
                                    bestApDay3 = apDay3;
                                    if(diff > 0){
                                        done = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(bestDay1 != -1){
            short temp = tripZones[bestDay3];
            tripZones[bestDay3] = tripZones[bestDay2];
            tripZones[bestDay2] = tripZones[bestDay1];
            tripZones[bestDay1] = temp;
            //---
            tripAirports[bestDay3] = bestApDay2;
            tripAirports[bestDay2] = bestApDay1;
            tripAirports[bestDay1] = bestApDay3;
            //---
//            System.out.print("  " + iter + ".  " + id + "\ttripleSwapFull: " + fitness + " -> " + (fitness-bestDiff) + "\n");
            fitness -= bestDiff;
        }
        //--- Is it an improving move?
        if(bestDiff > 0){
            nonImprovingMoves = 0;
        }
        else{
            nonImprovingMoves++;
        }

        return bestDiff;
    }
    
    public int saOpPerturbSwap(int iter, int id, int worstFitness, int bestFitness){
        int nbDefects = 0;
        int oldF = fitness;
        System.out.print("   " + iter + ".  " + id + "\tperturbance swap: " + fitness);
        if(bestEver > fitness){
            bestEver = fitness;
            bestEverTripAirports = Arrays.copyOf(tripAirports, tripAirports.length);
        }
        else if(fitness > bestEver+10000){
            tripAirports = Arrays.copyOf(bestEverTripAirports, bestEverTripAirports.length);
            fitness = bestEver;
        }
        for(int r=0; r<Main.nbPerturb; r++){
            //--- choose a candidate day with large distToNeighbors
            short cand = (short)(1 + Main.rnd.nextInt(tripZones.length-2));
            int distToNeighborsCand = ReadData.flights[cand-1][tripAirports[cand-1]][tripAirports[cand]] 
                    + ReadData.flights[cand][tripAirports[cand]][tripAirports[cand+1]];

            //--- find a possible replacement
            int bestRepc = -1;
            int bestRepcValue = -1000000;
            int diff = 0;
            short k = (short)(Main.rnd.nextInt(tripZones.length-2));
            for(int j=0; j<tripZones.length; j++, k = (short)((k+1) % (tripZones.length-2))){
                short repc = (short)(k + 1);
                short newCandIn = ReadData.flights[repc-1][tripAirports[repc-1]][tripAirports[cand]];
                short newCandOut = ReadData.flights[repc][tripAirports[cand]][tripAirports[repc+1]];
                short newRepIn = ReadData.flights[cand-1][tripAirports[cand-1]][tripAirports[repc]];
                short newRepOut = ReadData.flights[cand][tripAirports[repc]][tripAirports[cand+1]];
                if(cand == repc 
                        || (nbDefects>0 && (newCandIn == ReadData.noFlightCost || newCandOut == ReadData.noFlightCost 
                                    || newRepIn == ReadData.noFlightCost || newRepOut == ReadData.noFlightCost))){
                    continue;
                }
                int distToNeighborsRepc = ReadData.flights[repc-1][tripAirports[repc-1]][tripAirports[repc]] 
                        + ReadData.flights[repc][tripAirports[repc]][tripAirports[repc+1]];
                diff = (distToNeighborsCand + distToNeighborsRepc) - (newCandIn + newCandOut + newRepIn + newRepOut);
                if(diff > bestRepcValue){
                    bestRepc = repc;
                    bestRepcValue = diff;
                }
            }
            //---
            if(bestRepc != -1){
                short temp = tripZones[cand];
                tripZones[cand] = tripZones[bestRepc];
                tripZones[bestRepc] = temp;
                temp = tripAirports[cand];
                tripAirports[cand] = tripAirports[bestRepc];
                tripAirports[bestRepc] = temp;
                fitness -= bestRepcValue;
                if(bestRepcValue < -10000){
                    nbDefects++;
                }
            }
        }
        //---
        System.out.println(" -> " + fitness + "\t\t* * * Perturbance");
        nonImprovingMoves = 0;
        return fitness-oldF;
    }

    public int saOpPerturbTripleSwap(int iter, int id, int worstFitness, int bestFitness){
        int diff = 0;
        int oldF = fitness;
        int nbDefects = 0;
        System.out.print("   " + iter + ".  " + id + "\tperturbance triple: " + fitness);
        if(bestEver > fitness){
            bestEver = fitness;
            bestEverTripAirports = Arrays.copyOf(tripAirports, tripAirports.length);
        }
        else if(fitness > bestEver+10000){
            tripAirports = Arrays.copyOf(bestEverTripAirports, bestEverTripAirports.length);
            fitness = bestEver;
        }
        
        for(int r=0; r<Main.nbPerturb; r++){
            short bestDay1 = -1;
            short bestDay2 = -1;
            short bestDay3 = -1;
            int bestDiff = -1000000;
            boolean done = false;
            //--- (1) choose a candidate day1 with large distToPrev
            short day1 = chooseDayByDistToPred(10);
            int distToNeighborsDay1 = ReadData.flights[day1-1][tripAirports[day1-1]][tripAirports[day1]] 
                    + ReadData.flights[day1][tripAirports[day1]][tripAirports[day1+1]];
            //--- (2) choose day2 so that any ap from day1 fits to day2
            short k = (short)(Main.rnd.nextInt(tripZones.length-2));    //--- start from a random day
            for(int j=0; j<tripZones.length/3 && !done; j++, k = (short)((k+1) % (tripZones.length-2))){
                short day2 = (short)(k + 1);
                if((ReadData.flights[day2-1][tripAirports[day2-1]][tripAirports[day1]] == ReadData.noFlightCost)
                   || (ReadData.flights[day2][tripAirports[day1]][tripAirports[day2+1]] == ReadData.noFlightCost) 
                   || Math.abs(day1-day2)<2){
                    continue;
                }
                int distToNeighborsDay2 = ReadData.flights[day2-1][tripAirports[day2-1]][tripAirports[day2]] 
                        + ReadData.flights[day2][tripAirports[day2]][tripAirports[day2+1]];
                //--- (3) choose day3 so that its ap fits to day1 
                //---     and ap from day2 fits to day3
                short m = (short)(Main.rnd.nextInt(tripZones.length-2));    //--- start from a random day
                for(int l=0; l<tripZones.length/3 && !done; l++, m = (short)((m+1) % (tripZones.length-2))){
                    short day3 = (short)(m + 1);
                    if((ReadData.flights[day1-1][tripAirports[day1-1]][tripAirports[day3]] == ReadData.noFlightCost)    //--- ap3 fits in day1
                       || (ReadData.flights[day1][tripAirports[day3]][tripAirports[day1+1]] == ReadData.noFlightCost)
                       || (ReadData.flights[day3-1][tripAirports[day3-1]][tripAirports[day2]] == ReadData.noFlightCost) //--- ap2 fits in day3
                       || (ReadData.flights[day3][tripAirports[day2]][tripAirports[day3+1]] == ReadData.noFlightCost)
                       || Math.abs(day1-day3)<2
                       || Math.abs(day2-day3)<2){
                        continue;
                    }
                    int distToNeighborsDay3 = ReadData.flights[day3-1][tripAirports[day3-1]][tripAirports[day3]] 
                            + ReadData.flights[day3][tripAirports[day3]][tripAirports[day3+1]];
                    //--- check move quality
                    short newInDay1 = ReadData.flights[day1-1][tripAirports[day1-1]][tripAirports[day3]];
                    short newOutDay1 = ReadData.flights[day1][tripAirports[day3]][tripAirports[day1+1]];
                    short newInDay2 = ReadData.flights[day2-1][tripAirports[day2-1]][tripAirports[day1]];
                    short newOutDay2 = ReadData.flights[day2][tripAirports[day1]][tripAirports[day2+1]];
                    short newInDay3 = ReadData.flights[day3-1][tripAirports[day3-1]][tripAirports[day2]];
                    short newOutDay3 = ReadData.flights[day3][tripAirports[day2]][tripAirports[day3+1]];
                    diff = (distToNeighborsDay1 + distToNeighborsDay2 + distToNeighborsDay3) - (newInDay1 + newOutDay1 + newInDay2 + newOutDay2 + newInDay3 + newOutDay3);
                    if(bestDiff < diff){
                        bestDiff = diff;
                        bestDay1 = day1;
                        bestDay2 = day2;
                        bestDay3 = day3;
                        if(diff > 0){
                            done = true;
                            break;
                        }
                    }
                }
            }
            if(bestDay1 != -1){
                short temp = tripZones[bestDay3];
                tripZones[bestDay3] = tripZones[bestDay2];
                tripZones[bestDay2] = tripZones[bestDay1];
                tripZones[bestDay1] = temp;
                //---
                temp = tripAirports[bestDay3];
                tripAirports[bestDay3] = tripAirports[bestDay2];
                tripAirports[bestDay2] = tripAirports[bestDay1];
                tripAirports[bestDay1] = temp;
                //---
                fitness -= bestDiff;
                if(bestDiff < -10000){
                    nbDefects++;
                }
            }
        }
        //---
        System.out.println(" -> " + fitness + "\t\t* * * Perturbance tripleSwap");
        nonImprovingMoves = 0;
        return fitness-oldF;
    }
    
    public boolean collisionElimination(){
        //--- find a collision dayId
        if(collisions.isEmpty()){
            return false;
        }
        //--- find unused zones
        ArrayList<Short> unusedZones = new ArrayList<>();
        boolean[] isUsed = new boolean[ReadData.nbZones];
        for(short i=0; i<tripZones.length; i++){
            if(tripZones[i] != -1){
                isUsed[tripZones[i]] = true;
            }
        }
        for(short i=0; i<isUsed.length; i++){
            if(!isUsed[i]){
                unusedZones.add((Short)i);
            }
        }
        for(short uz: unusedZones){
        }
        //--- Find zones suited to collision days
        for(short coll: collisions){
            short day = priorityList[coll];
            short apIn = tripAirports[day-1];
            short apOut = tripAirports[day+1];
            ArrayList<ZoneAirport> candidates = new ArrayList<>();
            //--- check all airports if they have flights
            //---   - on dayId-1 from apIn
            //---   - on dayId to apOut
            for(short ap=0; ap<ReadData.nbAirports; ap++){
                short costIn, costOut;
                if((costIn=ReadData.flights[day-1][apIn][ap]) != ReadData.noFlightCost && (costOut=ReadData.flights[day][ap][apOut]) != ReadData.noFlightCost){
                    candidates.add(new ZoneAirport(ReadData.airport2zone[ap], ap, (short)-1, (short)-1, (short)(costIn+costOut)));
                }
            }
        }
        return true;
    }
   
    /**
     * EA operator.
     * Input: priorityList.
     * Constructs the whole trip and calculates its cost.
     * @return 
     */
    public int constructTrip(){
//        Main.rnd.setSeed(seed);
        fitness = 0;
        int penalty = 100000;
        collisions = new ArrayList<>();
        for(short i=1; i<ReadData.nbZones; i++){
            tripZones[i] = -1;
            tripAirports[i] = -1;
        }
        //---
        ArrayList<Short> availableZones = new ArrayList<>();
        for(short i=0; i<ReadData.nbZones; i++){
            availableZones.add(i);
        }
        availableZones.remove(new Short(startZone));
        //---
        for(short dayId=0; dayId<priorityList.length; dayId++){
            //--- A) in desert
            if(tripZones[priorityList[dayId]-1] == -1 && tripZones[priorityList[dayId]+1] == -1){
                ZoneAirport za = getBestStopoverInDesert(priorityList[dayId], availableZones);
                if(za != null){
                    tripZones[priorityList[dayId]] = za.zone;
                    tripAirports[priorityList[dayId]] = za.airport;
                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                }
                else{
                    fitness += penalty;
                    collisions.add((Short)dayId);
                }
            }
            //--- B) zoneFrom is given
            else if(tripZones[priorityList[dayId]-1] != -1 && tripZones[priorityList[dayId]+1] == -1){
                ZoneAirport za = getMostReachableStopoverFrom(priorityList[dayId], tripZones[priorityList[dayId]-1], tripAirports[priorityList[dayId]-1], availableZones);
                if(za != null){
                    tripZones[priorityList[dayId]] = za.zone;
                    tripAirports[priorityList[dayId]] = za.airport;
                    fitness += za.cost;
                    if(tripAirports[priorityList[dayId]-1] == -1){
                        tripAirports[priorityList[dayId]-1] = za.airportFrom;
                    }
                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                }
                else{
//                    tripZones[priorityList[dayId]] = availableZones.get(Main.rnd.nextInt(availableZones.size()));
//                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                    fitness += penalty;
                    collisions.add((Short)dayId);
                }
            }
            //--- C) zoneTo is given
            else if(tripZones[priorityList[dayId]-1] == -1 && tripZones[priorityList[dayId]+1] != -1){
                ZoneAirport za = getMostReachableStopoverTo(priorityList[dayId], tripZones[priorityList[dayId]+1], tripAirports[priorityList[dayId]+1], availableZones);
                if(za != null){
                    tripZones[priorityList[dayId]] = za.zone;
                    tripAirports[priorityList[dayId]] = za.airport;
                    fitness += za.cost;
                    if(tripAirports[priorityList[dayId]+1] == -1){
                        tripAirports[priorityList[dayId]+1] = za.airportTo;
                    }
                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                }
                else{
//                    tripZones[priorityList[dayId]] = availableZones.get(Main.rnd.nextInt(availableZones.size()));
//                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                    fitness += penalty;
                    collisions.add((Short)dayId);
                }
            }
            //--- D) zoneFrom and zoneTo are given
            else if(tripZones[priorityList[dayId]-1] != -1 && tripZones[priorityList[dayId]+1] != -1){
                ZoneAirport za = getBestStopoverBetween(priorityList[dayId], tripZones[priorityList[dayId]-1], tripAirports[priorityList[dayId]-1], tripZones[priorityList[dayId]+1], tripAirports[priorityList[dayId]+1], availableZones);
                if(za != null){
                    tripZones[priorityList[dayId]] = za.zone;
                    tripAirports[priorityList[dayId]] = za.airport;
                    fitness += za.cost;
                    if(tripAirports[priorityList[dayId]-1] == -1){
                        tripAirports[priorityList[dayId]-1] = za.airportFrom;
                    }
                    if(tripAirports[priorityList[dayId]+1] == -1){
                        tripAirports[priorityList[dayId]+1] = za.airportTo;
                    }
                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                }
                else{
//                    tripZones[priorityList[dayId]] = availableZones.get(Main.rnd.nextInt(availableZones.size()));
//                    availableZones.remove(new Short(tripZones[priorityList[dayId]]));
                    fitness += penalty;
                    collisions.add((Short)dayId);
                }
            }
        }
        return fitness;
    }

    /**
     * Finds a stopover za on the way to given airportTo in zoneTo, that
    - has the largest number of possible incoming connection zones
    - and minimizes the cost of the outgoing flight
     * @param day
     * @param zoneTo
     * @param airportTo ... absolute airport index
     * @param availableZones
     * @return 
     */
    public ZoneAirport getMostReachableStopoverTo(short day, short zoneTo, short airportTo, ArrayList<Short> availableZones){
        ZoneAirport res = null;
        ArrayList<ZoneAirport> candidates = new ArrayList<>();
        if(toShuffle){
            Collections.shuffle(availableZones);
        }

        //--- find all candidate pairs za-airport, that has connection to zoneTo-airportTo
        //--- A) airportTo is given
        if(airportTo != -1){
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    short cost;
                    if((cost=ReadData.flights[day][ReadData.zoneAirports[zone][a]][airportTo]) != ReadData.noFlightCost){
                        candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], (short)(-1), (short)(-1), cost));
                        if(candidates.size() == candSize){
                            done = true;
                            break;
                        }
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- B) airportTo is NOT given
        else{
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    for(short ap_to=0; ap_to<ReadData.zoneAirports[zoneTo].length; ap_to++){
                        short cost;
                        if((cost=ReadData.flights[day][ReadData.zoneAirports[zone][a]][ap_to]) != ReadData.noFlightCost){
                            candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], (short)(-1), ap_to, cost));
                            if(candidates.size() == candSize){
                                done = true;
                                break;
                            }
                        }
                    }
                    if(done){
                        break;
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- calculate reachability of candidates
        if(!candidates.isEmpty()){
            for(ZoneAirport cand: candidates){
                for(Short zone_from: availableZones){
                    if(zone_from == cand.zone){
                        continue;
                    }
                    for(short ap_from=0; ap_from<ReadData.zoneAirports[zone_from].length; ap_from++){
                        short cost;
                        if((cost=ReadData.flights[day-1][ReadData.zoneAirports[zone_from][ap_from]][cand.airport]) != ReadData.noFlightCost){
                            cand.reachability++;
                            break;  //--- count just the za connectivity
                        }
                    }
                }
            }
            //--- choose the best one
            for(ZoneAirport cand: candidates){
                if(res == null 
                   || res.reachability < cand.reachability
                   || (res.reachability == cand.reachability && res.cost > cand.cost)){
                    res = cand;
                }
            }
        }
        return res;
    }

    /**
     * Finds a stopover za on the way from the given airportFrom in zoneFrom, that
    - has the largest number of possible outgoing connection zones
    - and minimizes the cost of the incoming flight
     * @param day
     * @param zoneTo
     * @param airportTo
     * @param availableZones
     * @return 
     */
    public ZoneAirport getMostReachableStopoverFrom(short day, short zoneFrom, short airportFrom, ArrayList<Short> availableZones){
        ZoneAirport res = null;
        ArrayList<ZoneAirport> candidates = new ArrayList<>();
        if(toShuffle){
            Collections.shuffle(availableZones);
        }

        //--- find all candidate pairs [zone_to, airport_to], to which there is a connection from [zoneFrom, airportFrom]
        //--- A) airportFrom is given
        if(airportFrom != -1){
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    short cost;
                    if((cost=ReadData.flights[day-1][airportFrom][ReadData.zoneAirports[zone][a]]) != ReadData.noFlightCost){
                        candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], (short)(-1), (short)(-1), cost));
                        if(candidates.size() == candSize){
                            done = true;
                            break;
                        }
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- B) airportFrom is NOT given
        else{
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    for(short a_from=0; a_from<ReadData.zoneAirports[zoneFrom].length; a_from++){
                        short cost;
                        if((cost=ReadData.flights[day-1][ReadData.zoneAirports[zoneFrom][a_from]][ReadData.zoneAirports[zone][a]]) != ReadData.noFlightCost){
                            candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], ReadData.zoneAirports[zoneFrom][a_from], (short)(-1), cost));
                            if(candidates.size() == candSize){
                                done = true;
                                break;
                            }
                        }
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- calculate reachability of candidates
        if(!candidates.isEmpty()){
            for(ZoneAirport cand: candidates){
                for(Short zone_to: availableZones){
                    if(zone_to == cand.zone){
                        continue;
                    }
                    for(short a_to=0; a_to<ReadData.zoneAirports[zone_to].length; a_to++){
                        short cost;
                        if((cost=ReadData.flights[day][cand.airport][ReadData.zoneAirports[zone_to][a_to]]) != ReadData.noFlightCost){
                            cand.reachability++;
                            break;  //--- count just the za connectivity
                        }
                    }
                }
            }
            //--- choose the best one
            for(ZoneAirport cand: candidates){
                if(res == null 
                   || res.reachability < cand.reachability
                   || (res.reachability == cand.reachability && res.cost > cand.cost)){
                    res = cand;
                }
            }
        }
        return res;
    }

    /**
     * Finds a stopover za on the way from the given [zoneFrom, airportFrom] to [zoneTo, airportTo] that
    - minimizes the total cost of the connecting flights
     * @param day
     * @param zoneFrom
     * @param airportFrom
     * @param zoneTo
     * @param airportTo
     * @param availableZones
     * @return 
     */
    public ZoneAirport getBestStopoverBetween(short day, short zoneFrom, short airportFrom, short zoneTo, short airportTo, ArrayList<Short> availableZones){
        ZoneAirport res = null;
        ArrayList<ZoneAirport> candidates = new ArrayList<>();
        if(toShuffle){
            Collections.shuffle(availableZones);
        }

        //--- find all candidate pairs [za, airport]
        //--- A) airportFrom and airportTo are given
        if(airportFrom != -1 && airportTo != -1){
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    short costFrom, costTo;
                    if((costFrom=ReadData.flights[day-1][airportFrom][ReadData.zoneAirports[zone][a]]) != ReadData.noFlightCost 
                       && (costTo=ReadData.flights[day][ReadData.zoneAirports[zone][a]][airportTo]) != ReadData.noFlightCost){
                        candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], (short)(-1), (short)(-1), (short)(costFrom+costTo)));
                        if(candidates.size() == candSize){
                            done = true;
                            break;
                        }
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- B) airportFrom is not given
        else if(airportFrom == -1 && airportTo != -1){
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    for(short a_from=0; a_from<ReadData.zoneAirports[zoneFrom].length; a_from++){
                        short costFrom, costTo;
                        if((costFrom=ReadData.flights[day-1][ReadData.zoneAirports[zoneFrom][a_from]][ReadData.zoneAirports[zone][a]]) != ReadData.noFlightCost
                           && (costTo=ReadData.flights[day][ReadData.zoneAirports[zone][a]][airportTo]) != ReadData.noFlightCost){
                            candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], ReadData.zoneAirports[zoneFrom][a_from], (short)(-1), (short)(costFrom+costTo)));
                            if(candidates.size() == candSize){
                                done = true;
                                break;
                            }
                        }
                    }
                    if(done){
                        break;
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- C) airportTo is not given
        else if(airportFrom != -1 && airportTo == -1){
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    for(short a_to=0; a_to<ReadData.zoneAirports[zoneTo].length; a_to++){
                        short costFrom, costTo;
                        if((costFrom=ReadData.flights[day-1][airportFrom][ReadData.zoneAirports[zone][a]]) != ReadData.noFlightCost
                           && (costTo=ReadData.flights[day][ReadData.zoneAirports[zone][a]][ReadData.zoneAirports[zoneTo][a_to]]) != ReadData.noFlightCost){
                            candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], (short)(-1), ReadData.zoneAirports[zoneTo][a_to], (short)(costFrom+costTo)));
                            if(candidates.size() == candSize){
                                done = true;
                                break;
                            }
                        }
                    }
                    if(done){
                        break;
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- D) neither airportFrom nor airportTo is given
        else{
            boolean done = false;
            for(Short zone: availableZones){
                for(short a=0; a<ReadData.zoneAirports[zone].length; a++){
                    for(short a_from=0; a_from<ReadData.zoneAirports[zoneFrom].length; a_from++){
                        for(short a_to=0; a_to<ReadData.zoneAirports[zoneTo].length; a_to++){
                            short costFrom, costTo;
                            if((costFrom=ReadData.flights[day-1][ReadData.zoneAirports[zoneFrom][a_from]][ReadData.zoneAirports[zone][a]]) != ReadData.noFlightCost
                               && (costTo=ReadData.flights[day][ReadData.zoneAirports[zone][a]][ReadData.zoneAirports[zoneTo][a_to]]) != ReadData.noFlightCost){
                                candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], ReadData.zoneAirports[zoneFrom][a_from], ReadData.zoneAirports[zoneTo][a_to], (short)(costFrom+costTo)));
                                if(candidates.size() == candSize){
                                    done = true;
                                    break;
                                }
                            }                            
                        }
                        if(done){
                            break;
                        }
                    }
                    if(done){
                        break;
                    }
                }
                if(done){
                    break;
                }
            }
        }
        //--- choose the best one
        for(ZoneAirport cand: candidates){
            if(res == null 
               || res.cost > cand.cost){
                res = cand;
            }
        }
        return res;
    }

    /**
     * Finds the best stopover za with no incoming or outgoing za specified that
    - maximizes its reachability from both sides
    - at least one connection in both directions 
     * @param day
     * @param availableZones
     * @return chosen za id
     */
    public ZoneAirport getBestStopoverInDesert(short day, ArrayList<Short> availableZones){
        short nbOK = 0;
        short maxOK = 3;
        ArrayList<ZoneAirport> candidates = new ArrayList<>();
        if(toShuffle){
            Collections.shuffle(availableZones);
        }

        //--- find all candidate zones
        boolean done = false;
        for(Short zone: availableZones){
            boolean exists = false;
            for(short a=0; a<ReadData.zoneAirports[zone].length && !exists; a++){
                for(Short zone_from: availableZones){
                    if(zone_from == zone || exists){
                        break;
                    }
                    for(short a_from=0; a_from<ReadData.zoneAirports[zone_from].length && !exists; a_from++){
                        for(Short zone_to: availableZones){
                            if(zone_to == zone || exists){
                                break;
                            }
                            for(short a_to=0; a_to<ReadData.zoneAirports[zone_to].length; a_to++){
                                if(ReadData.flights[day-1][ReadData.zoneAirports[zone_from][a_from]][ReadData.zoneAirports[zone][a]] != ReadData.noFlightCost
                                   && ReadData.flights[day][ReadData.zoneAirports[zone][a]][ReadData.zoneAirports[zone_to][a_to]] != ReadData.noFlightCost){
                                    candidates.add(new ZoneAirport(zone, ReadData.zoneAirports[zone][a], (short)-1, (short)-1, (short)-1));
                                    exists = true;
                                    nbOK++;
                                    if(nbOK == maxOK){
                                        done = true;
                                    }
                                    break;
                                }
                                if(done){
                                    break;
                                }
                            }
                            if(done){
                                break;
                            }
                        }
                        if(done){
                            break;
                        }
                    }
                    if(done){
                        break;
                    }
                }
                if(done){
                    break;
                }
            }
            if(done){
                break;
            }
        }
        //--- choose one randomly
        if(candidates.isEmpty()){
            return null;
        }
        else{
            return candidates.get(Main.rnd.nextInt(candidates.size()));
        }
    }
    
    public void printSolution(){
//        ArrayList<String> fromTo = new ArrayList<>();
        boolean[] visited = new boolean[ReadData.nbZones];
//        if(collisions.isEmpty()){
            System.out.println(fitness);
            int fitnessCheck = 0;
            for(short d=0; d<tripZones.length-1; d++){
                short cost = ReadData.flights[d][tripAirports[d]][tripAirports[d+1]];
//                System.out.println(ReadData.airports.get(tripAirports[d]) + " " + ReadData.airports.get(tripAirports[d+1]) + " " + (d+1) + " " + cost);
                System.out.println(ReadData.airports.get(tripAirports[d]) + "(" + tripAirports[d] + ") " + ReadData.airports.get(tripAirports[d+1]) + "(" + tripAirports[d+1] + ") " + (d+1) + " " + cost);
                fitnessCheck += cost;
//                if(cost > 50){
//                    fromTo.add(d + "\t" + tripAirports[d] + "\t" + tripAirports[d+1]);
//                }
                if(ReadData.airport2zone[tripAirports[d]] == ReadData.airport2zone[tripAirports[d+1]]){
                    System.out.println("  ! ! ! Blbost1: " + ReadData.airports.get(tripAirports[d]) + " / " + ReadData.airports.get(tripAirports[d+1]));
                }
                visited[ReadData.airport2zone[tripAirports[d]]] = true;
            }
//            System.out.println(fitness + " / " + fitnessCheck);
            for(short d=0; d<tripZones.length-1; d++){
                if(!visited[d]){
                    System.out.println("! ! ! Blbost2: " + d);
                }
            }
            System.out.println(fitness);
//        }
//        System.out.println("\nLong flights:");
//        for(int i=0; i<fromTo.size(); i++){
//            System.out.println(fromTo.get(i));
//        }
    }

    public boolean checkFitness(){
        boolean[] visited = new boolean[ReadData.nbZones];
//        if(collisions.isEmpty()){
            int fitnessCheck = 0;
            for(short d=0; d<tripZones.length-1; d++){
                short cost = ReadData.flights[d][tripAirports[d]][tripAirports[d+1]];
                fitnessCheck += cost;
                visited[ReadData.airport2zone[tripAirports[d]]] = true;
            }
            for(short d=0; d<tripZones.length-1; d++){
                if(!visited[d]){
                    System.out.println("! ! ! checkFitness Blbost2: " + d);
                }
            }
            if(fitness != fitnessCheck){
                System.out.println("   ! ! ! Fitness check FALSE: " + fitness + " / " + fitnessCheck);
            }
            else{
                System.out.println("   fitness is OK: " + fitness + " / " + fitnessCheck);
            }
//        }
        return false;
    }
}
