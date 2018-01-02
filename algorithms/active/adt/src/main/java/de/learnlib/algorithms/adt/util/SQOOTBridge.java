/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.adt.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.model.ObservationTree;
import de.learnlib.api.oracle.SymbolQueryOracle;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;

/**
 * A utility class that links an observation tree with a symbol query oracle, meaning that all queries to the symbol
 * query oracle will be stored in the observation tree. Additionally, if a query can be answered by the observation tree
 * (and caching is enabled) the delegated symbol query oracle will not be queried.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class SQOOTBridge<I, O> implements SymbolQueryOracle<I, O> {

    private final FastMealy<I, O> observationTree;

    private final SymbolQueryOracle<I, O> delegate;

    private final boolean enableCache;

    private final List<I> currentTrace;

    private FastMealyState<O> currentState;

    private boolean currentTraceValid;

    public SQOOTBridge(final ObservationTree<?, I, O> observationTree,
                       final SymbolQueryOracle<I, O> delegate,
                       final boolean enableCache) {
        this.observationTree = observationTree.getObservationTree();
        this.delegate = delegate;
        this.enableCache = enableCache;
        this.currentTrace = new ArrayList<>();
    }

    public void initialize() {
        this.currentState = this.observationTree.getInitialState();
        this.currentTraceValid = enableCache;
    }

    @Override
    public O query(I i) {

        if (this.currentTraceValid) {
            final FastMealyState<O> succ = this.observationTree.getSuccessor(this.currentState, i);

            if (succ != null) {
                final O output = this.observationTree.getOutput(this.currentState, i);
                this.currentTrace.add(i);
                this.currentState = succ;
                return output;
            } else {
                this.currentTraceValid = false;
                this.delegate.reset();

                for (final I trace : this.currentTrace) {
                    this.delegate.query(trace);
                }
            }
        }

        final O output = this.delegate.query(i);

        final FastMealyState<O> nextState;
        final FastMealyState<O> succ = this.observationTree.getSuccessor(this.currentState, i);

        if (succ == null) {
            final FastMealyState<O> newState = this.observationTree.addState();
            this.observationTree.addTransition(this.currentState, i, newState, output);
            nextState = newState;
        } else {
            assert this.observationTree.getOutput(this.currentState, i).equals(output) : "Inconsistent observations";
            nextState = succ;
        }

        this.currentState = nextState;

        return output;
    }

    @Override
    public void reset() {
        this.currentState = this.observationTree.getInitialState();

        if (this.enableCache) {
            this.currentTrace.clear();
            this.currentTraceValid = true;
        } else {
            this.delegate.reset();
        }
    }
}
