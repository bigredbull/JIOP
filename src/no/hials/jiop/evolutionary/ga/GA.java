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
package no.hials.jiop.evolutionary.ga;

import java.util.ArrayList;
import java.util.List;
import no.hials.jiop.base.MLMethod;
import no.hials.jiop.base.candidates.Candidate;
import no.hials.jiop.factories.AbstractCandidateFactory;

/**
 *
 * @author LarsIvar
 * @param <E>
 */
public class GA<E> extends MLMethod<E> {

    private final SelectionOperator<E> selection;
    private final CrossoverOperator<E> crossover;
    private final MutationOperator<E> mutation;

    private final int numElites;
    private final int numSelection;
    private final int numMutations;
    
    public GA(int size, float elitism, float keep, float mutrate, int candiateLength, SelectionOperator<E> selection, CrossoverOperator<E> crossover, MutationOperator<E> mutation, AbstractCandidateFactory<E> generator) { 
        super(size, candiateLength, generator);
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.numElites = Math.round(size*elitism);
        this.numSelection = Math.round(size*keep);
        this.numMutations = Math.round((size-numElites)*mutrate);
    }

    public GA(int size, int numElites, int numSelection, int numMutations, int candiateLength, SelectionOperator<E> selection, CrossoverOperator<E> crossover, MutationOperator<E> mutation, AbstractCandidateFactory<E> generator) {
        super(size, candiateLength, generator);
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.numElites = numElites;
        this.numSelection = numSelection;
        this.numMutations = numMutations;
    }

    @Override
    protected void doIteration() {
        sortCandidates();
        List<Candidate<E>> elites = getElites();
        
        final List<Candidate<E>> selected = selection.selectCandidates(this, numSelection);
        final List<Candidate<E>> offspring = crossover.createoffspring(selected, Math.round((size-numElites-numSelection)/2));
        List<Candidate<E>> newpop = new ArrayList<>(selected.size()+offspring.size());
        newpop.addAll(selected);
        newpop.addAll(offspring);
        mutation.mutateCandidates(newpop, numMutations);
        clear();
        addAll(elites);
        addAll(newpop);
        while(size() < size) {
            add(getFactory().generateRandom(candidateLength));
        }
        updateCost();
    }

    private List<Candidate<E>> getElites() {
        List<Candidate<E>> elites = new ArrayList<>(numElites);
        for (int i = 0; i < numElites; i++) {
            Candidate<E> c = new Candidate<>(get(i));
            elites.add(c);
        }
        return elites;
    }

    @Override
    public String getName() {
       return "Genetic Algorithm";
    }

}
