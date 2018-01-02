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
package de.learnlib.filter.reuse.test;

import com.google.common.base.Supplier;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.filter.reuse.ReuseCapableOracle;
import de.learnlib.filter.reuse.ReuseOracle;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Similar to the {@link LearningTest} but this time with quiescence in outputs. The purpose of this test is just to
 * check that the reuse filter is able to work with {@code null} outputs.
 *
 * @author Oliver Bauer
 */
public class QuiescenceTest {

    private ReuseOracle<Integer, Integer, String> reuseOracle;
    private Alphabet<Integer> sigma;

    /**
     * {@inheritDoc}.
     */
    @BeforeClass
    protected void setUp() {
        sigma = Alphabets.integers(0, 3);
        reuseOracle = new ReuseOracle.ReuseOracleBuilder<>(sigma, new TestOracleFactory()).build();
    }

    @Test
    public void simpleTest() {
        MealyLearner<Integer, String> learner =
                new ExtensibleLStarMealyBuilder<Integer, String>().withAlphabet(sigma).withOracle(reuseOracle).create();

        learner.startLearning();
    }

    private class TestOracleFactory implements Supplier<ReuseCapableOracle<Integer, Integer, String>> {

        @Override
        public ReuseCapableOracle<Integer, Integer, String> get() {
            return new TestOracle();
        }

    }

    class TestOracle implements ReuseCapableOracle<Integer, Integer, String> {

        private final int threshold = 3;

        @Override
        public QueryResult<Integer, String> continueQuery(Word<Integer> trace, Integer s) {

            Integer integer = s;

            WordBuilder<String> output = new WordBuilder<>();
            for (Integer symbol : trace) {
                if (integer + symbol < threshold) {
                    integer += symbol;
                    output.add("ok");
                } else if (integer + symbol == threshold) {
                    integer += symbol;
                    output.add("done");
                } else {
                    output.add(null); // quiescence
                }
            }

            QueryResult<Integer, String> result;
            result = new QueryResult<>(output.toWord(), integer);

            return result;
        }

        @Override
        public QueryResult<Integer, String> processQuery(Word<Integer> trace) {
            Integer integer = 0;
            WordBuilder<String> output = new WordBuilder<>();
            for (Integer symbol : trace) {
                if (integer + symbol < threshold) {
                    integer += symbol;
                    output.add("ok");
                } else if (integer + symbol == threshold) {
                    integer += symbol;
                    output.add("done");
                } else {
                    output.add(null); // quiescence
                }
            }

            QueryResult<Integer, String> result;
            result = new QueryResult<>(output.toWord(), integer);

            return result;
        }
    }
}
