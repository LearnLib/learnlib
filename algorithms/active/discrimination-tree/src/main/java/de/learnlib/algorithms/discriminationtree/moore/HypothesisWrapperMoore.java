/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.algorithms.discriminationtree.moore;

import java.util.Collection;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link MooreMachine}-based specialization of the DT learner hypothesis.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author bayram
 * @author frohme
 */
class HypothesisWrapperMoore<I, O>
        implements MooreMachine<HState<I, Word<O>, O, Void>, I, HTransition<I, Word<O>, O, Void>, O> {

    private final DTLearnerHypothesis<I, Word<O>, O, Void> dtHypothesis;

    HypothesisWrapperMoore(DTLearnerHypothesis<I, Word<O>, O, Void> dtHypothesis) {
        this.dtHypothesis = dtHypothesis;
    }

    @Override
    public O getStateOutput(HState<I, Word<O>, O, Void> state) {
        return state.getProperty();
    }

    @Override
    public Collection<HState<I, Word<O>, O, Void>> getStates() {
        return dtHypothesis.getStates();
    }

    @Override
    public @Nullable HState<I, Word<O>, O, Void> getInitialState() {
        return dtHypothesis.getInitialState();
    }

    @Override
    public @Nullable HTransition<I, Word<O>, O, Void> getTransition(HState<I, Word<O>, O, Void> state, I input) {
        return dtHypothesis.getTransition(state, input);
    }

    @Override
    public HState<I, Word<O>, O, Void> getSuccessor(HTransition<I, Word<O>, O, Void> transition) {
        return dtHypothesis.getSuccessor(transition);
    }
}