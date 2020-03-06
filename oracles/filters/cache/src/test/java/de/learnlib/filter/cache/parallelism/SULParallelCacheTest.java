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

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.cache.LearningCacheOracle.MealyLearningCacheOracle;
import de.learnlib.filter.cache.SULLearningCacheOracle;
import de.learnlib.filter.cache.sul.SULCache;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public class SULParallelCacheTest
        extends AbstractParallelCacheTest<MealyMachine<?, Character, ?, Character>, Character, Word<Character>> {

    @Override
    protected Alphabet<Character> getAlphabet() {
        return Alphabets.characters('a', 'e');
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTargetModel() {
        return RandomAutomata.randomMealy(new Random(42), MODEL_SIZE, getAlphabet(), getAlphabet());
    }

    @Override
    protected MealyLearningCacheOracle<Character, Character> getCache(Alphabet<Character> alphabet,
                                                                      MembershipOracle<Character, Word<Character>> oracle) {
        return SULLearningCacheOracle.fromSULCache(SULCache.createTreeCache(alphabet,
                                                                            new TestSUL<>(getTargetModel(), oracle)));
    }

    protected static class TestSUL<I, O> extends MealySimulatorSUL<I, O> {

        private final WordBuilder<I> wb;
        private final MembershipOracle<I, Word<O>> oracle;
        private final MealyMachine<?, I, ?, O> mealyMachine;

        TestSUL(MealyMachine<?, I, ?, O> mealy, MembershipOracle<I, Word<O>> oracle) {
            super(mealy);
            this.mealyMachine = mealy;
            this.oracle = oracle;
            this.wb = new WordBuilder<>();
        }

        @Override
        public void pre() {
            wb.clear();
            super.pre();
        }

        @Override
        public O step(I in) {
            wb.add(in);
            return super.step(in);
        }

        @Override
        public void post() {
            // query oracle to update counters
            oracle.answerQuery(wb.toWord());
            wb.clear();
            super.post();
        }

        @Override
        public boolean canFork() {
            return true;
        }

        @Override
        public SUL<I, O> fork() {
            return new TestSUL<>(mealyMachine, oracle);
        }
    }
}
