/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.hials.jiop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.hials.utilities.DoubleArray;
import no.hials.utilities.NormUtil;

/**
 *
 * @author LarsIvar
 */
public class DifferentialEvolution extends Algorithm implements Optimizable {

    private final Object mutex = new Object();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final ExecutorCompletionService completionService = new ExecutorCompletionService(pool);

    private int NP;
    private double F, CR;
    private Candidates candidates;

    private boolean multiCore;
    private Candidate bestCandidate;

    public DifferentialEvolution(int NP, double F, double CR, boolean multiCore) {
        super("Differential Evolution " + multiCore);
        this.F = F;
        this.CR = CR;
        this.NP = NP;
        this.multiCore = multiCore;
    }

    @Override
    public void subInit() {
        this.candidates = new Candidates(NP);
        Collections.sort(candidates);
        this.bestCandidate = candidates.get(0).copy();
    }

    @Override
    public void subInit(DoubleArray... seeds) {
        this.candidates = new Candidates(NP - seeds.length);
        for (DoubleArray seed : seeds) {
            candidates.add(new Candidate(seed, getEvaluator().evaluate(seed)));
        }
        Collections.sort(candidates);
        this.bestCandidate = candidates.get(0).copy();
    }

    @Override
    protected Candidate singleIteration() {
        
        if (multiCore) {
            for (final Candidate c : candidates) {
                completionService.submit(new Runnable() {

                    @Override
                    public void run() {
                        DoubleArray c1, c2, c3;
                        do {
                            int rand = rng.nextInt(candidates.size());
                            c1 = candidates.get(rand);
                        } while (c1 == c);
                        do {
                            int rand = rng.nextInt(candidates.size());
                            c2 = candidates.get(rand);
                        } while (c2 == c && c2 == c1);
                        do {
                            int rand = rng.nextInt(candidates.size());
                            c3 = candidates.get(rand);
                        } while (c3 == c && c3 == c1 && c3 == c2);
                        int R = rng.nextInt(getDimension());
                        DoubleArray array = new DoubleArray(getDimension());
                        for (int i = 0; i < array.length; i++) {
                            if ((rng.nextDouble() < CR) || (i == R)) {
                                double value = c1.get(i) + F * (c2.get(i) - c3.get(i));
                                array.set(i, value);
                            } else {
                                array.set(i, c.get(i));
                            }
                        }
                        array.clamp(0, 1);
                        Candidate sample = new Candidate(array, getEvaluator().evaluate(array));
                        if (sample.getCost() < c.getCost()) {
                            candidates.set(candidates.indexOf(c), sample);
                            synchronized (mutex) {
                                if (sample.getCost() < bestCandidate.getCost()) {
                                    bestCandidate = sample;
                                }
                            }
                        }
                    }
                }, true);
            }
            for (Candidate c : candidates) {
                try {
                    completionService.take().get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(DifferentialEvolution.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            for (Candidate c : candidates) {
                DoubleArray p = c;
                DoubleArray p1, p2, p3;
                do {
                    int rand = rng.nextInt(candidates.size());
                    p1 = candidates.get(rand);
                } while (p1 == p);
                do {
                    int rand = rng.nextInt(candidates.size());
                    p2 = candidates.get(rand);
                } while (p2 == p && p2 == p1);
                do {
                    int rand = rng.nextInt(candidates.size());
                    p3 = candidates.get(rand);
                } while (p3 == p && p3 == p1 && p3 == p2);
                int R = rng.nextInt(c.length);
                DoubleArray array = new DoubleArray(c.length);
                for (int i = 0; i < array.length; i++) {
                    if ((rng.nextDouble() < CR) || (i == R)) {
                        double value = p1.get(i) + F * (p2.get(i) - p3.get(i));
                        array.set(i, value);
                    } else {
                        array.set(i, p.get(i));
                    }
                }
                array.clamp(0, 1);
                Candidate sample = new Candidate(array, getEvaluator().evaluate(array));
                if (sample.getCost() < c.getCost()) {
                    candidates.set(candidates.indexOf(c), sample);
                    if (sample.getCost() < bestCandidate.getCost()) {
                        bestCandidate = sample;
                    }
                }
            }
        }

        return bestCandidate.copy();
    }

    public int getNP() {
        return NP;
    }

    public void setNP(int NP) {
        this.NP = NP;
    }

    public double getF() {
        return F;
    }

    public void setF(double F) {
        this.F = F;
    }

    public double getCR() {
        return CR;
    }

    public void setCR(double CR) {
        this.CR = CR;
    }

    @Override
    public int getNumberOfFreeParameters() {
        return 3;
    }

    @Override
    public void setFreeParameters(DoubleArray array) {
        this.NP = (int) new NormUtil(1, 0, 60, 10).normalize(array.get(0));
        this.F = new NormUtil(1, 0, 2, 0.1).normalize(array.get(1));
        this.CR = new NormUtil(1, 0, 1, 0.1).normalize(array.get(2));
    }
    
    @Override
    public DoubleArray getFreeParameters() {
        return new DoubleArray(NP,F,CR);
    }

    private class Candidates extends ArrayList<Candidate> {

        public Candidates(int size) {
            super(size);
            for (int i = 0; i < size; i++) {
                add(Candidate.randomCandidate(getDimension(), getEvaluator()));
            }
        }

    }
}
