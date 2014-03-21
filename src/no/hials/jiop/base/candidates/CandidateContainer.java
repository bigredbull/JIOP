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
package no.hials.jiop.base.candidates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lars Ivar Hatledal
 * @param <E> the type
 */
public class CandidateContainer<E> extends ArrayList<Candidate<E>>{

    /**
     * Initiliazes the list
     * @param size the size to initialize to
     */
    public CandidateContainer(int size) {
       super(size);
    }

    /**
     * Performs a Collections.sort()
     * @return this
     */
    public CandidateContainer<E> sort() {
        Collections.sort(this);
        return this;
    }

    /**
     * Clears all candidates and inserts the new one
     * @param candidates the candidates to insert into the cleared list
     */
    public void clearAndAddAll(List<Candidate<E>> candidates) {
        this.clear();
        this.addAll(candidates);
    }

    /**
     * Getter for the average cost in this list
     * @return the average cost
     */
    public double getAverage() {
        double avg = 0;
        for (Candidate<E> c : this) {
            avg += c.getCost();
        }
        return avg / size();
    }

    /**
     * Getter for the best candidates in this list
     * @param numBest the number of candidates to get
     * @return the best candidates
     */
    public List<Candidate<E>> getBestCandidates(int numBest) {
        if (numBest > size()) {
            throw new IllegalArgumentException("The number of best candidates are greater than the current number of candidates! Desired: " + numBest + ", Available: " + size());
        } else if (numBest <= 0) {
            throw new IllegalArgumentException("The number of best candidates are less or equal to 0!");
        }
        List<Candidate<E>> elites = new ArrayList<>(this);
        Collections.sort(elites);
        elites.subList(numBest, elites.size()).clear();
        return elites;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Candidate c : this) {
            sb.append(c);
            if (i++ != size() - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
