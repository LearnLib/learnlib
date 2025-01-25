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
package de.learnlib.testsupport;

import java.util.Random;

import de.learnlib.Resumable;
import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

public abstract class AbstractResumableLearnerMealyTest<L extends Resumable<T> & LearningAlgorithm<MealyMachine<?, Character, ?, Character>, Character, Word<Character>>, T>
        extends AbstractResumableLearnerTest<L, MealyMachine<?, Character, ?, Character>, MealyMembershipOracle<Character, Character>, Character, Word<Character>, T> {

    private static final int AUTOMATON_SIZE = 20;

    @Override
    protected Alphabet<Character> getInitialAlphabet() {
        return Alphabets.characters('1', '4');
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTarget(Alphabet<Character> alphabet) {
        return RandomAutomata.randomMealy(new Random(RANDOM_SEED),
                                          AUTOMATON_SIZE,
                                          alphabet,
                                          Alphabets.characters('a', 'd'));
    }

    @Override
    protected MealyMembershipOracle<Character, Character> getOracle(MealyMachine<?, Character, ?, Character> target) {
        return new MealySimulatorOracle<>(target);
    }
}
