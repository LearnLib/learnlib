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
package de.learnlib.algorithms.ttt.dfa;

import de.learnlib.algorithms.ttt.base.AbstractTTTHypothesis;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

public class TTTHypothesisDFA<I> extends AbstractTTTHypothesis<I, Boolean, TTTState<I, Boolean>>
        implements DFA<TTTState<I, Boolean>, I>,
                   UniversalDeterministicAutomaton.FullIntAbstraction<TTTState<I, Boolean>, Boolean, Void> {

    public TTTHypothesisDFA(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public TTTState<I, Boolean> getSuccessor(TTTState<I, Boolean> transition) {
        return transition;
    }

    @Override
    protected TTTState<I, Boolean> mapTransition(TTTTransition<I, Boolean> internalTransition) {
        return internalTransition.getTarget();
    }

    @Override
    protected TTTState<I, Boolean> newState(int alphabetSize, TTTTransition<I, Boolean> parent, int id) {
        return new TTTStateDFA<>(alphabet.size(), parent, id);
    }

    @Override
    public UniversalDeterministicAutomaton.FullIntAbstraction<TTTState<I, Boolean>, Boolean, Void> fullIntAbstraction(
            Alphabet<I> alphabet) {
        if (alphabet == this.alphabet) {
            return this;
        }
        return DFA.super.fullIntAbstraction(alphabet);
    }

    @Override
    public Boolean getStateProperty(int state) {
        return isAccepting(states.get(state));
    }

    @Override
    public boolean isAccepting(TTTState<I, Boolean> state) {
        if (!(state instanceof TTTStateDFA)) {
            throw new IllegalArgumentException("State is not an expected DFA state, but " + state);
        }
        return ((TTTStateDFA<I>) state).accepting;
    }

    @Override
    public Void getTransitionProperty(TTTState<I, Boolean> transition) {
        return null;
    }
}
