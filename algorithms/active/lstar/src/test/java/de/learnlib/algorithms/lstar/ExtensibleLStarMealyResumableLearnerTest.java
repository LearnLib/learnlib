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
package de.learnlib.algorithms.lstar;

import java.util.Random;

import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.AbstractResumableLearnerMealyTest;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author bainczyk
 */
public class ExtensibleLStarMealyResumableLearnerTest
        extends AbstractResumableLearnerMealyTest<ExtensibleLStarMealy<Character, Character>, AutomatonLStarState<Character, Word<Character>, CompactMealy<Character, Character>, Integer>> {

    @Override
    protected ExtensibleLStarMealy<Character, Character> getLearner(final MembershipOracle<Character, Word<Character>> oracle,
                                                                    final Alphabet<Character> alphabet) {
        return new ExtensibleLStarMealyBuilder<Character, Character>().withAlphabet(alphabet)
                                                                      .withOracle(oracle)
                                                                      .create();
    }

    @Override
    protected int getRounds() {
        return 2;
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTarget(final Alphabet<Character> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), 100, alphabet, Alphabets.characters('a', 'd'));
    }
}
