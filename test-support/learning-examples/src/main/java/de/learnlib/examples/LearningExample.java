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
package de.learnlib.examples;

import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.spa.SPA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.VPDAlphabet;

public interface LearningExample<I, A> {

    A getReferenceAutomaton();

    Alphabet<I> getAlphabet();

    interface UniversalDeterministicLearningExample<I, A extends UniversalAutomaton<?, I, ?, ?, ?>>
            extends LearningExample<I, A> {}

    interface DFALearningExample<I> extends UniversalDeterministicLearningExample<I, DFA<?, I>> {}

    interface MealyLearningExample<I, O> extends UniversalDeterministicLearningExample<I, MealyMachine<?, I, ?, O>> {}

    interface SSTLearningExample<I, O>
            extends UniversalDeterministicLearningExample<I, SubsequentialTransducer<?, I, ?, O>> {}

    /**
     * A {@link LearningExample} refinement for {@link StateLocalInputMealyMachine}.
     * <p>
     * Note that while {@link StateLocalInputMealyMachine}s can return information about their {@link
     * StateLocalInputMealyMachine#getLocalInputs local inputs} and are therefore in general partially defined, the
     * examples are total {@link MealyMachine}s in order to be usable with the existing integration-test infrastructure
     * of (total) {@link MealyMachine}s. The 'undefined' transitions are answered with {@link #getUndefinedOutput()}.
     */
    interface StateLocalInputMealyLearningExample<I, O>
            extends UniversalDeterministicLearningExample<I, StateLocalInputMealyMachine<?, I, ?, O>> {

        O getUndefinedOutput();

    }

    interface SPALearningExample<I> extends LearningExample<I, SPA<?, I>> {

        @Override
        SPAAlphabet<I> getAlphabet();
    }

    interface OneSEVPALearningExample<I> extends LearningExample<I, OneSEVPA<?, I>> {

        @Override
        VPDAlphabet<I> getAlphabet();
    }

}
