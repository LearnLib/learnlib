/* Copyright (C) 2013-2020 TU Dortmund
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
package de.learnlib.filter.cache.parallelism;

import java.util.Random;

import de.learnlib.api.oracle.parallelism.ParallelOracle;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public class MealyParallelCacheTest
        extends AbstractParallelCacheTest<MealyCounterOracle<Character, Character>, MealyLearningCacheOracle<Character, Character>, MealyMachine<?, Character, ?, Character>, Character, Word<Character>> {

    @Override
    protected Alphabet<Character> getAlphabet() {
        return Alphabets.characters('a', 'e');
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTargetModel(Alphabet<Character> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), MODEL_SIZE, getAlphabet(), getAlphabet());
    }

    @Override
    protected MealyCounterOracle<Character, Character> getSUL(MealyMachine<?, Character, ?, Character> targetModel) {
        return new MealyCounterOracle<>(new MealySimulatorOracle<>(targetModel), "Queries");
    }

    @Override
    protected MealyLearningCacheOracle<Character, Character> getCache(Alphabet<Character> alphabet,
                                                                      MealyCounterOracle<Character, Character> sul) {
        return MealyCaches.createCache(alphabet, sul);
    }

    @Override
    protected ParallelOracle<Character, Word<Character>> getParallelOracle(MealyLearningCacheOracle<Character, Character> cache) {
        return ParallelOracleBuilders.newDynamicParallelOracle(() -> cache).create();
    }

    @Override
    protected int getNumberOfQueries(MealyCounterOracle<Character, Character> model) {
        return (int) model.getStatisticalData().getCount();
    }
}
