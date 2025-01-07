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
package de.learnlib.datastructure.pta.wrapper;

import java.util.Collection;

import de.learnlib.datastructure.pta.AbstractBasePTAState;
import de.learnlib.datastructure.pta.BasePTA;
import de.learnlib.datastructure.pta.PTATransition;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.FiniteAlphabetAutomaton;
import net.automatalib.automaton.transducer.MealyMachine;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MealyWrapper<S extends AbstractBasePTAState<S, Void, O>, I, O>
        implements MealyMachine<S, I, PTATransition<S>, O>, FiniteAlphabetAutomaton<S, I, PTATransition<S>> {

    private final Alphabet<I> alphabet;
    private final BasePTA<S, Void, O> pta;

    public MealyWrapper(Alphabet<I> alphabet, BasePTA<S, Void, O> pta) {
        this.alphabet = alphabet;
        this.pta = pta;
    }

    @Override
    public Alphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public O getTransitionOutput(PTATransition<S> transition) {
        return pta.getTransitionProperty(transition);
    }

    @Override
    public Collection<S> getStates() {
        return pta.getStates();
    }

    @Override
    public @Nullable PTATransition<S> getTransition(S state, I input) {
        return pta.getTransition(state, alphabet.getSymbolIndex(input));
    }

    @Override
    public S getSuccessor(PTATransition<S> transition) {
        return pta.getSuccessor(transition);
    }

    @Override
    public S getInitialState() {
        return pta.getInitialState();
    }
}
