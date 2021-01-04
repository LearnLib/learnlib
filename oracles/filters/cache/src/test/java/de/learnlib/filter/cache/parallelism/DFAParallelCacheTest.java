/* Copyright (C) 2013-2021 TU Dortmund
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
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.dfa.DFACaches;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public class DFAParallelCacheTest
        extends AbstractParallelCacheTest<DFACounterOracle<Character>, DFALearningCacheOracle<Character>, DFA<?, Character>, Character, Boolean> {

    @Override
    protected Alphabet<Character> getAlphabet() {
        return Alphabets.characters('a', 'e');
    }

    @Override
    protected DFA<?, Character> getTargetModel(Alphabet<Character> alphabet) {
        return RandomAutomata.randomDFA(new Random(42), MODEL_SIZE, getAlphabet());
    }

    @Override
    protected DFACounterOracle<Character> getSUL(DFA<?, Character> targetModel) {
        return new DFACounterOracle<>(new DFASimulatorOracle<>(targetModel), "Queries");
    }

    @Override
    protected DFALearningCacheOracle<Character> getCache(Alphabet<Character> alphabet,
                                                         DFACounterOracle<Character> sul) {
        return DFACaches.createCache(alphabet, sul);
    }

    @Override
    protected ParallelOracle<Character, Boolean> getParallelOracle(DFALearningCacheOracle<Character> cache) {
        return ParallelOracleBuilders.newDynamicParallelOracle(() -> cache).create();
    }

    @Override
    protected int getNumberOfQueries(DFACounterOracle<Character> model) {
        return (int) model.getCount();
    }
}
