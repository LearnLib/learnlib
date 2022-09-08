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
package de.learnlib.algorithms.ttt.dfa;

import de.learnlib.algorithms.ttt.base.AbstractTTTHypothesis;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

public class TTTHypothesisDFA<I> extends AbstractTTTHypothesis<TTTStateDFA<I>, I, Boolean, TTTStateDFA<I>>
        implements DFA<TTTStateDFA<I>, I>, FullIntAbstraction<TTTStateDFA<I>, Boolean, Void> {

    public TTTHypothesisDFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public TTTStateDFA<I> getSuccessor(TTTStateDFA<I> transition) {
        return transition;
    }

    @Override
    protected TTTStateDFA<I> mapTransition(TTTTransition<I, Boolean> internalTransition) {
        return (TTTStateDFA<I>) internalTransition.getTarget();
    }

    @Override
    protected TTTStateDFA<I> newState(int alphabetSize, TTTTransition<I, Boolean> parent, int id) {
        return new TTTStateDFA<>(numInputs(), parent, id);
    }

    @Override
    public UniversalDeterministicAutomaton.FullIntAbstraction<TTTStateDFA<I>, Boolean, Void> fullIntAbstraction(Alphabet<I> alphabet) {
        if (alphabet.equals(getInputAlphabet())) {
            return this;
        }
        return DFA.super.fullIntAbstraction(alphabet);
    }

    @Override
    public Boolean getStateProperty(int state) {
        return isAccepting(states.get(state));
    }

    @Override
    public boolean isAccepting(TTTStateDFA<I> state) {
        return state.accepting;
    }

    @Override
    public Void getTransitionProperty(TTTStateDFA<I> transition) {
        return null;
    }
}
