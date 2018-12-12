
import java.util.ArrayList;
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
public class EAFinalEvalThread implements Runnable {
    ArrayList<Individual> pop;
    int first, last;
    
    CountDownLatch latch;

    public EAFinalEvalThread(ArrayList<Individual> inds, int first, int last, CountDownLatch latch){
        pop = inds;
        this.first = first;
        this.last = last;
        this.latch = latch;
    }
    
    public void run() {
        for(int i=first; i<last; i++){
            pop.get(i).calculateTripPrice(true);
            //--- Fine-tuning
            for(short lsIter=0; lsIter<50; lsIter++){
                if(Main.rnd.nextDouble() < 0.9){
                    pop.get(i).saOpSwap(lsIter, i, pop.get(i).fitness, pop.get(i).fitness);
                }
                else{
                    pop.get(i).saOpTripleSwap(lsIter, i, pop.get(i).fitness, pop.get(i).fitness);
                }
            }
        }
        latch.countDown();
    }
}