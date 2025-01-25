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
package de.learnlib.algorithm.ttt.mealy;

import de.learnlib.algorithm.ttt.base.AbstractTTTHypothesis;
import de.learnlib.algorithm.ttt.base.TTTState;
import de.learnlib.algorithm.ttt.base.TTTTransition;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.UniversalDeterministicAutomaton.FullIntAbstraction;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

public class TTTHypothesisMealy<I, O>
        extends AbstractTTTHypothesis<TTTState<I, Word<O>>, I, Word<O>, TTTTransitionMealy<I, O>> implements
                                                                                                  MealyMachine<TTTState<I, Word<O>>, I, TTTTransitionMealy<I, O>, O>,
                                                                                                  FullIntAbstraction<TTTTransitionMealy<I, O>, Void, O> {

    public TTTHypothesisMealy(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public TTTState<I, Word<O>> getSuccessor(TTTTransitionMealy<I, O> transition) {
        return transition.getTarget();
    }

    @Override
    protected TTTTransitionMealy<I, O> mapTransition(TTTTransition<I, Word<O>> internalTransition) {
        return (TTTTransitionMealy<I, O>) internalTransition;
    }

    @Override
    protected TTTState<I, Word<O>> newState(int alphabetSize, TTTTransition<I, Word<O>> parent, int id) {
        return new TTTState<>(alphabetSize, parent, id);
    }

    @Override
    public UniversalDeterministicAutomaton.FullIntAbstraction<TTTTransitionMealy<I, O>, Void, O> fullIntAbstraction(
            Alphabet<I> alphabet) {
        if (alphabet.equals(getInputAlphabet())) {
            return this;
        }
        return MealyMachine.super.fullIntAbstraction(alphabet);
    }

    @Override
    public O getTransitionOutput(TTTTransitionMealy<I, O> transition) {
        return transition.getOutput();
    }

    @Override
    public Void getStateProperty(int state) {
        return null;
    }

    @Override
    public O getTransitionProperty(TTTTransitionMealy<I, O> transition) {
        return transition.getOutput();
    }
}
