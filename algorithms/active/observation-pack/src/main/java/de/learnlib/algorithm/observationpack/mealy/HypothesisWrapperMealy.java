/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.algorithm.observationpack.mealy;

import java.util.Collection;

import de.learnlib.algorithm.observationpack.hypothesis.HState;
import de.learnlib.algorithm.observationpack.hypothesis.HTransition;
import de.learnlib.algorithm.observationpack.hypothesis.OPLearnerHypothesis;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

final class HypothesisWrapperMealy<I, O>
        implements MealyMachine<HState<I, Word<O>, Void, O>, I, HTransition<I, Word<O>, Void, O>, O> {

    private final OPLearnerHypothesis<I, Word<O>, Void, O> dtHypothesis;

    HypothesisWrapperMealy(OPLearnerHypothesis<I, Word<O>, Void, O> dtHypothesis) {
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
