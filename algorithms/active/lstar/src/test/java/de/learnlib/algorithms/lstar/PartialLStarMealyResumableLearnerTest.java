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
package de.learnlib.algorithms.lstar;

import java.util.Random;

import de.learnlib.algorithms.lstar.mealy.PartialLStarMealy;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealyBuilder;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.driver.util.StateLocalInputMealySimulatorSUL;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.testsupport.AbstractResumableLearnerTest;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public class PartialLStarMealyResumableLearnerTest
        extends AbstractResumableLearnerTest<PartialLStarMealy<Integer, Character>, StateLocalInputMealyMachine<?, Integer, ?, Character>, StateLocalInputMealyOracle<Integer, OutputAndLocalInputs<Integer, Character>>, Integer, Word<OutputAndLocalInputs<Integer, Character>>, AutomatonLStarState<Integer, Word<OutputAndLocalInputs<Integer, Character>>, CompactMealy<Integer, Character>, Integer>> {

    @Override
    protected Alphabet<Integer> getInitialAlphabet() {
        return Alphabets.integers(1, 5);
    }

    @Override
    protected StateLocalInputMealyMachine<?, Integer, ?, Character> getTarget(Alphabet<Integer> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), 100, alphabet, Alphabets.characters('a', 'd'));
    }

    @Override
    protected StateLocalInputMealyOracle<Integer, OutputAndLocalInputs<Integer, Character>> getOracle(
            StateLocalInputMealyMachine<?, Integer, ?, Character> target) {
        return new StateLocalInputSULOracle<>(new StateLocalInputMealySimulatorSUL<>(target));
    }

    @Override
    protected EquivalenceOracle<StateLocalInputMealyMachine<?, Integer, ?, Character>, Integer, Word<OutputAndLocalInputs<Integer, Character>>> getEquivalenceOracle(
            StateLocalInputMealyMachine<?, Integer, ?, Character> target) {
        return new StateLocalInputMealySimulatorEQOracle<>(target);
    }

    @Override
    protected PartialLStarMealy<Integer, Character> getLearner(StateLocalInputMealyOracle<Integer, OutputAndLocalInputs<Integer, Character>> oracle,
                                                               Alphabet<Integer> alphabet) {
        return new PartialLStarMealyBuilder<Integer, Character>().withOracle(oracle).create();
    }

    @Override
    protected int getRounds() {
        return 2;
    }
}
