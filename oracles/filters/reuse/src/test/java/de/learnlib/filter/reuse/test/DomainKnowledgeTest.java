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
import com.google.common.collect.Sets;
import de.learnlib.filter.reuse.ReuseCapableOracle;
import de.learnlib.filter.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filter.reuse.ReuseOracle;
import de.learnlib.filter.reuse.tree.ReuseNode.NodeResult;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Reuse oracle test that uses invariant input symbols.
 *
 * @author Oliver Bauer
 */
public class DomainKnowledgeTest {

    private ReuseOracle<Integer, Integer, String> reuseOracle;

    /**
     * {@inheritDoc}.
     */
    @BeforeMethod
    protected void setUp() {
        // We don't use this oracle, we directly test against the reuse tree!
        Alphabet<Integer> alphabet = Alphabets.integers(0, 10);

        reuseOracle = new ReuseOracle.ReuseOracleBuilder<>(alphabet,
                                                           new NullReuseCapableFactory()).withInvariantInputs(Sets.newHashSet(
                0)).build();
    }

    @Test
    public void testPumpSymbolsSimple() {
        // Add one entry (0,ok) where 0 is model invariant (reflexive edge)
        QueryResult<Integer, String> qr = new QueryResult<>(getOutput("ok"), 0);
        reuseOracle.getReuseTree().insert(getInput(0), qr);

        Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(0, 0, 0, 0));
        Assert.assertNotNull(known);
        Assert.assertEquals(known.size(), 4);
        Assert.assertEquals(known, getOutput("ok", "ok", "ok", "ok"));
    }

    private static Word<String> getOutput(String... param) {
        return Word.fromSymbols(param);
    }

    private static Word<Integer> getInput(Integer... param) {
        return Word.fromSymbols(param);
    }

    @Test(dependsOnMethods = {"testPumpSymbolsSimple"})
    public void testPumpSymbolsComplex() {
        // Add one entry (101,ok1 ok0 ok1) where 0 is model invariant
        QueryResult<Integer, String> qr = new QueryResult<>(getOutput("ok1", "ok0", "ok1"), 2);
        reuseOracle.getReuseTree().insert(getInput(1, 0, 1), qr);

        Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(1, 0, 0, 0, 0, 1));
        Assert.assertNotNull(known);
        Assert.assertEquals(known.size(), 6);
        Assert.assertEquals(known, getOutput("ok1", "ok0", "ok0", "ok0", "ok0", "ok1"));
    }

    @Test
    public void testReuseNodePrefixWhileReusing() {
        QueryResult<Integer, String> qr = new QueryResult<>(getOutput("ok", "ok", "ok"), 2);
        reuseOracle.getReuseTree().insert(getInput(1, 0, 1), qr);

        Word<Integer> input = getInput(1, 0, 1, 1);
        NodeResult<Integer, Integer, String> node = reuseOracle.getReuseTree().fetchSystemState(input);

        Assert.assertTrue(node.prefixLength == 3); // ''1 0 1''
        // reuse the prefix
        qr = new QueryResult<>(getOutput("ok"), 3);
        reuseOracle.getReuseTree().insert(getInput(1), node.reuseNode, qr);

        // The "1 1" system state should not be available:
        node = reuseOracle.getReuseTree().fetchSystemState(getInput(1, 1));
        Assert.assertNull(node);

        // There should be a "1 1 1" system state, even this query was never seen
        node = reuseOracle.getReuseTree().fetchSystemState(getInput(1, 1, 1));
        Assert.assertNotNull(node);
        Assert.assertTrue(node.prefixLength == 3); // query "1 1 1"

        // The system state is invalidated, so querying again "1 1 1" reveals null this time
        node = reuseOracle.getReuseTree().fetchSystemState(getInput(1, 1, 1));
        Assert.assertNull(node);

        // The output of "1 0 0 0 0 1 1" should be known, even the query was never seen
        Word<String> output = reuseOracle.getReuseTree().getOutput(getInput(1, 0, 0, 0, 0, 1, 1));
        Assert.assertNotNull(output);
        Assert.assertTrue(output.size() == 7);
    }

    private class NullReuseCapableFactory implements Supplier<ReuseCapableOracle<Integer, Integer, String>> {

        @Override
        public ReuseCapableOracle<Integer, Integer, String> get() {
            return new ReuseCapableOracle<Integer, Integer, String>() {

                @Override
                public QueryResult<Integer, String> continueQuery(Word<Integer> trace, Integer integer) {
                    return null;
                }

                @Override
                public QueryResult<Integer, String> processQuery(Word<Integer> trace) {
                    return null;
                }
            };
        }
    }

}
