/* Copyright (C) 2017 TU Dortmund
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
package de.learnlib.algorithms.lstargeneric;

import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.MembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerMealyTest;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import java.util.Random;

/**
 * @author bainczyk
 */
public class ExtensibleLStarMealyResumableLearnerTest extends AbstractResumableLearnerMealyTest<
        ExtensibleLStarMealy<Integer, Character>,
        AutomatonLStarState<Integer, Word<Character>, CompactMealy<Integer, Character>, Integer>> {

    @Override
    protected ExtensibleLStarMealy<Integer, Character> getLearner(final MembershipOracle<Integer, Word<Character>> oracle,
                                                                  final Alphabet<Integer> alphabet) {
        return new ExtensibleLStarMealyBuilder<Integer, Character>()
                .withAlphabet(alphabet)
                .withOracle(oracle)
                .create();
    }

    @Override
    protected MealyMachine<?, Integer, ?, Character> getTarget(final Alphabet<Integer> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), 100, alphabet, Alphabets.characters('a', 'd'));
    }

    @Override
    protected int getRounds() {
        return 2;
    }
}
