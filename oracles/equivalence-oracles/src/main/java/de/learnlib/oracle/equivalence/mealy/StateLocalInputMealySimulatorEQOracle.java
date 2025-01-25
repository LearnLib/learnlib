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
package de.learnlib.oracle.equivalence.mealy;

import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.transducer.MealyMachines;

public class StateLocalInputMealySimulatorEQOracle<I, O> extends MealySimulatorEQOracle<I, O>
        implements MealyEquivalenceOracle<I, O> {

    public StateLocalInputMealySimulatorEQOracle(MealyMachine<?, I, ?, O> reference,
                                                 Alphabet<I> alphabet,
                                                 O undefinedOutput) {
        super(MealyMachines.complete(reference, alphabet, undefinedOutput));
    }
}
