/* Copyright (C) 2013-2019 TU Dortmund
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
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.words.Alphabet;

public interface LearningExample<I, A extends UniversalAutomaton<?, I, ?, ?, ?>> {

    A getReferenceAutomaton();

    Alphabet<I> getAlphabet();

    interface DFALearningExample<I> extends LearningExample<I, DFA<?, I>> {}

    interface MealyLearningExample<I, O> extends LearningExample<I, MealyMachine<?, I, ?, O>> {}

    interface StateLocalInputMealyLearningExample<I, O>
            extends LearningExample<I, StateLocalInputMealyMachine<?, I, ?, O>> {}

}
