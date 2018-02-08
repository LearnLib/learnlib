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
package de.learnlib.algorithms.discriminationtree.mealy;

import java.util.Collection;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

final class HypothesisWrapperMealy<I, O>
        implements MealyMachine<HState<I, Word<O>, Void, O>, I, HTransition<I, Word<O>, Void, O>, O> {

    private final DTLearnerHypothesis<I, Word<O>, Void, O> dtHypothesis;

    HypothesisWrapperMealy(DTLearnerHypothesis<I, Word<O>, Void, O> dtHypothesis) {
        this.dtHypothesis = dtHypothesis;
    }

    @Override
    public HState<I, Word<O>, Void, O> getSuccessor(HTransition<I, Word<O>, Void, O> trans) {
        return dtHypothesis.getSuccessor(trans);
    }

    @Override
    public Collection<HState<I, Word<O>, Void, O>> getStates() {
        return dtHypothesis.getStates();
    }

    @Override
    public HState<I, Word<O>, Void, O> getInitialState() {
        return dtHypothesis.getInitialState();
    }

    @Override
    public HTransition<I, Word<O>, Void, O> getTransition(HState<I, Word<O>, Void, O> state, I input) {
        return dtHypothesis.getTransition(state, input);
    }

    @Override
    public O getTransitionOutput(HTransition<I, Word<O>, Void, O> trans) {
        return trans.getProperty();
    }

}
