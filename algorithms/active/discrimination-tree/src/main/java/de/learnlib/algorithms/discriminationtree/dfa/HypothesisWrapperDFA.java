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
package de.learnlib.algorithms.discriminationtree.dfa;

import java.util.Collection;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import net.automatalib.automata.fsa.DFA;

final class HypothesisWrapperDFA<I> implements DFA<HState<I, Boolean, Boolean, Void>, I> {

    private final DTLearnerHypothesis<I, Boolean, Boolean, Void> dtHypothesis;

    HypothesisWrapperDFA(DTLearnerHypothesis<I, Boolean, Boolean, Void> dtHypothesis) {
        this.dtHypothesis = dtHypothesis;
    }

    @Override
    public Collection<HState<I, Boolean, Boolean, Void>> getStates() {
        return dtHypothesis.getStates();
    }

    @Override
    public HState<I, Boolean, Boolean, Void> getInitialState() {
        return dtHypothesis.getInitialState();
    }

    @Override
    public HState<I, Boolean, Boolean, Void> getTransition(HState<I, Boolean, Boolean, Void> state, I input) {
        return dtHypothesis.getSuccessor(state, input);
    }

    @Override
    public boolean isAccepting(HState<I, Boolean, Boolean, Void> state) {
        return state.getProperty();
    }

}
