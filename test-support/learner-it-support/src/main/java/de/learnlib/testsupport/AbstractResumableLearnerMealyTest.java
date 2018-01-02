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
package de.learnlib.testsupport;

import java.io.Serializable;
import java.util.Random;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.algorithm.feature.ResumableLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author bainczyk
 */
public abstract class AbstractResumableLearnerMealyTest<L extends ResumableLearner<T> & LearningAlgorithm<MealyMachine<?, Character, ?, Character>, Character, Word<Character>>, T extends Serializable>
        extends AbstractResumableLearnerTest<L, MealyMachine<?, Character, ?, Character>, MembershipOracle<Character, Word<Character>>, Character, Word<Character>, T> {

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
    protected MembershipOracle<Character, Word<Character>> getOracle(MealyMachine<?, Character, ?, Character> target) {
        return new SimulatorOracle<>(target);
    }
}
