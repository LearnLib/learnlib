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
package de.learnlib.algorithms.ttt.moore;

import de.learnlib.algorithms.ttt.base.AbstractTTTHypothesis;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automata.transducers.MooreMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * A {@link MooreMachine}-based specialization of the TTT hypothesis.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author bayram
 * @author frohme
 */
public class TTTHypothesisMoore<I, O>
        extends AbstractTTTHypothesis<TTTStateMoore<I, O>, I, Word<O>, TTTStateMoore<I, O>>
        implements MooreMachine<TTTStateMoore<I, O>, I, TTTStateMoore<I, O>, O>,
                   FullIntAbstraction<TTTStateMoore<I, O>, O, Void> {

    /**
     * Constructor.
     *
     * @param alphabet
     *         the input alphabet
     */
    public TTTHypothesisMoore(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    protected TTTStateMoore<I, O> mapTransition(TTTTransition<I, Word<O>> internalTransition) {
        return (TTTStateMoore<I, O>) internalTransition.getTarget();
    }

    @Override
    public O getStateProperty(int state) {
        TTTStateMoore<I, O> mooreState = states.get(state);
        return mooreState.getOutput();

    }

    @Override
    protected TTTStateMoore<I, O> newState(int alphabetSize, TTTTransition<I, Word<O>> parent, int id) {
        return new TTTStateMoore<>(alphabetSize, parent, id);
    }

    @Override
    public UniversalDeterministicAutomaton.FullIntAbstraction<TTTStateMoore<I, O>, O, Void> fullIntAbstraction(Alphabet<I> alphabet) {
        if (alphabet.equals(getInputAlphabet())) {
            return this;
        }
        return MooreMachine.super.fullIntAbstraction(alphabet);
    }

    @Override
    public O getStateOutput(TTTStateMoore<I, O> state) {
        return state.getOutput();
    }

    @Override
    public TTTStateMoore<I, O> getSuccessor(TTTStateMoore<I, O> transition) {
        return transition;
    }

    @Override
    public Void getTransitionProperty(TTTStateMoore<I, O> state) {
        return null;
    }
}