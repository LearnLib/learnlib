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

import java.io.IOException;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.filter.reuse.ReuseCapableOracle;
import de.learnlib.filter.reuse.ReuseOracle;
import de.learnlib.filter.reuse.tree.ReuseTree;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Simple learning test that shows who to use the reuse oracle.
 *
 * @author Oliver Bauer
 */
public class LearningTest {

    private ReuseOracle<Integer, Integer, String> reuseOracle;
    private Alphabet<Integer> sigma;

    /**
     * {@inheritDoc}.
     */
    @BeforeClass
    protected void setUp() {
        sigma = Alphabets.integers(0, 3);

        reuseOracle =
                new ReuseOracle.ReuseOracleBuilder<>(sigma, new TestOracleFactory()).withFailureOutputs(Sets.newHashSet(
                        "error")).withInvariantInputs(Sets.newHashSet(0)).build();
    }

    @Test
    public void simpleTest() throws IOException {

        MealyLearner<Integer, String> learner =
                new ExtensibleLStarMealyBuilder<Integer, String>().withAlphabet(sigma).withOracle(reuseOracle).create();

        learner.startLearning();

        ReuseTree<Integer, Integer, String> reuseTree = reuseOracle.getReuseTree();

        Appendable sb = new StringBuffer();
        GraphDOT.write(reuseTree, sb);
        Assert.assertTrue(sb.toString().startsWith("digraph g"));
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
                if (integer + symbol <= threshold) {
                    integer += symbol;
                    output.add("ok");
                } else {
                    output.add("error");
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
                if (integer + symbol <= threshold) {
                    integer += symbol;
                    output.add("ok");
                } else {
                    output.add("error");
                }
            }

            QueryResult<Integer, String> result;
            result = new QueryResult<>(output.toWord(), integer);

            return result;
        }
    }
}