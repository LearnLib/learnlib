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
package de.learnlib.testsupport;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.ResumableLearner;
import de.learnlib.api.SupportsGrowingAlphabet;
import de.learnlib.oracles.SimulatorOracle;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import java.io.Serializable;
import java.util.Collection;
import java.util.Random;

/**
 * @author bainczyk
 */
public abstract class AbstractResumableLearnerMealyTest<L extends ResumableLearner<T> & LearningAlgorithm<MealyMachine<?, Integer, ?, Character>, Integer, Word<Character>>, T extends Serializable>
        extends AbstractResumableLearnerTest<
        L,
        MealyMachine<?, Integer, ?, Character>,
        MembershipOracle<Integer, Word<Character>>,
        Integer,
        Word<Character>,
        T> {

    @Override
    protected Alphabet<Integer> getInitialAlphabet() {
        return Alphabets.integers(1, 4);
    }

    @Override
    protected MealyMachine<?, Integer, ?, Character> getTarget(Alphabet<Integer> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), 20, alphabet, Alphabets.characters('a', 'd'));
    }

    @Override
    protected MembershipOracle<Integer, Word<Character>> getOracle(MealyMachine<?, Integer, ?, Character> target) {
        return new SimulatorOracle<>(target);
    }
}
