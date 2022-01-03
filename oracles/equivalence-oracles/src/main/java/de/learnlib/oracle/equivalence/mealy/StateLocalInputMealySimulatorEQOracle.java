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
package de.learnlib.oracle.equivalence.mealy;

import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.util.automata.transducers.MealyMachines;
import net.automatalib.words.Alphabet;

public class StateLocalInputMealySimulatorEQOracle<I, O> extends MealySimulatorEQOracle<I, O>
        implements MealyEquivalenceOracle<I, O> {

    public StateLocalInputMealySimulatorEQOracle(MealyMachine<?, I, ?, O> reference,
                                                 Alphabet<I> alphabet,
                                                 O undefinedOutput) {
        super(MealyMachines.complete(reference, alphabet, undefinedOutput));
    }
}
