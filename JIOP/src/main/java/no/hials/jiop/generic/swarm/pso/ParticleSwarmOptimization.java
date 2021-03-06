/*
 * Copyright (c) 2014, Aalesund University College 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package no.hials.jiop.generic.swarm.pso;

import java.util.logging.Level;
import java.util.logging.Logger;
import no.hials.jiop.generic.Evaluator;
import no.hials.jiop.generic.GeneralPopulationBasedAlgorithm;
import no.hials.jiop.util.NormalizationUtility;
import no.hials.jiop.generic.candidates.Candidate;
import no.hials.jiop.generic.candidates.NumericCandidate;
import no.hials.jiop.generic.candidates.particles.ParticleCandidate;
import no.hials.jiop.generic.factories.NumericCandidateFactory;
import no.hials.jiop.generic.tuning.Optimizable;


/**
 *
 * @author Lars Ivar Hatledal
 * @param <E>
 */
public class ParticleSwarmOptimization<E> extends GeneralPopulationBasedAlgorithm<E> implements Optimizable {

    public double omega = 0.729, c1 = 1.49445, c2 = 1.49445, maxVel = 0.26;

    private final boolean multiThreaded;

    public ParticleSwarmOptimization(int size, NumericCandidateFactory<E> candidateFactory, Evaluator<E> evaluator, boolean multiThreaded) {
        super(size, candidateFactory, evaluator, multiThreaded ? "MultiThreaded Particle Swarm Optimization" : "SingleThreaded Particle Swarm Optimization");
        this.multiThreaded = multiThreaded;
    }

    public ParticleSwarmOptimization(int size, NumericCandidateFactory<E> candidateFactory, Evaluator<E> evaluator, String name, boolean multiThreaded) {
        super(size, candidateFactory, evaluator, name);
        this.multiThreaded = multiThreaded;
    }

    public ParticleSwarmOptimization(int size, double omega, double c1, double c2, double maxVel, NumericCandidateFactory<E> candidateFactory, Evaluator<E> evaluator, boolean multiThreaded) {
        this(size, candidateFactory, evaluator, multiThreaded ? "MultiThreaded Particle Swarm Optimization" : "SingleThreaded Particle Swarm Optimization", multiThreaded);
    }

    public ParticleSwarmOptimization(int size, double omega, double c1, double c2, double maxVel,NumericCandidateFactory<E> candidateFactory, Evaluator<E> evaluator, String name, boolean multiThreaded) {
        super(size, candidateFactory, evaluator, name);
        this.multiThreaded = multiThreaded;
        this.omega = omega;
        this.c1 = c1;
        this.c2 = c2;
        this.maxVel = maxVel;
    }

    @Override
    protected void singleIteration() {
        for (Candidate<E> p : population) {
            if (multiThreaded) {
                getCompletionService().submit(() -> threadingTask((ParticleCandidate<E>) p), null);
            } else {
                threadingTask((ParticleCandidate<E>) p);
            }
        }
        if (multiThreaded) {
            for (Candidate<E> p : population) {
                try {
                    getCompletionService().take();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ParticleSwarmOptimization.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void threadingTask(final ParticleCandidate<E> particle) {
        for (int i = 0; i < getDimension(); i++) {
            double li = particle.getLocalBest().get(i).doubleValue();
            double gi = ((NumericCandidate<E>) getBestCandidate()).get(i).doubleValue();
            double pi = particle.get(i).doubleValue();
            double vi = particle.getVelocityAt(i).doubleValue();

            double newVel = (omega * vi) + (rng.nextDouble() * c1 * (li - pi)) + (rng.nextDouble() * c2 * (gi - pi));

            if (Math.abs(newVel) > maxVel) {
                newVel = newVel > 0 ? maxVel : -maxVel;
            }

            double newPos = pi + newVel;
            if (newPos < 0) {
                newPos = 0;
            } else if (newPos > 1) {
                newPos = 1;
            }
            particle.set(i, newPos);
            particle.setVelocityAt(i, newVel);
        }
        evaluate(particle);
        double cost = particle.getCost();
        if (cost < particle.getLocalBest().getCost()) {
            particle.setLocalBest((NumericCandidate<E>) (particle).copy());
            setBestCandidateIfBetter(particle);
        }

    }

    public double getOmega() {
        return omega;
    }

    public void setOmega(double omega) {
        this.omega = omega;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    public double getMaxVel() {
        return maxVel;
    }

    public void setMaxVel(double maxVel) {
        this.maxVel = maxVel;

    }

    @Override
    public int getNumberOfFreeParameters() {
        return 5;
    }

    @Override
    public void setFreeParameters(double[] array) {
        setInitialSize((int) new NormalizationUtility(1, 0, 60, 10).normalize(array[0]));
        this.omega = new NormalizationUtility(1, 0, 1, 0.01).normalize(array[1]);
        this.c1 = new NormalizationUtility(1, 0, 2, 0.01).normalize(array[2]);
        this.c2 = new NormalizationUtility(1, 0, 2, 0.01).normalize(array[3]);
        this.maxVel = new NormalizationUtility(1, 0, 1, 0.0001).normalize(array[4]);
    }

    @Override
    public double[] getFreeParameters() {
        return new double[]{size(), omega, c1, c2, maxVel};
    }
}
