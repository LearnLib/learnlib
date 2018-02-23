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
package de.learnlib.filter.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import net.automatalib.automata.concepts.Output;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A simple test against various cache implementations.
 *
 * @author Oliver Bauer
 * @author frohme
 */
public abstract class AbstractCacheTest<A extends Output<I, D>, I, D> {

    private static final int LENGTH = 5;
    private Random random = new Random(42);
    private Alphabet<I> alphabet;
    private LearningCacheOracle<A, I, D> oracle;
    private List<Query<I, D>> queries;

    @BeforeClass
    public void setup() {
        alphabet = getAlphabet();
        oracle = getCachedOracle();
        queries = new ArrayList<>();
    }

    @Test
    public void testNoQueriesReceived() {
        Assert.assertEquals(queries.size(), 0);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 0);
    }

    @Test(dependsOnMethods = {"testNoQueriesReceived"})
    public void testFirstQuery() {
        queries.add(new DefaultQuery<>(generateWord()));

        Assert.assertEquals(queries.size(), 1);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 1);
    }

    @Test(dependsOnMethods = {"testFirstQuery"})
    public void testFirstDuplicate() {
        Assert.assertEquals(queries.size(), 1);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 1);
    }

    @Test(dependsOnMethods = {"testFirstDuplicate"})
    public void testTwoQueriesOneDuplicate() {
        queries.add(new DefaultQuery<>(generateWord()));
        Assert.assertEquals(queries.size(), 2);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 2);
    }

    @Test(dependsOnMethods = {"testTwoQueriesOneDuplicate"})
    public void testOneNewQuery() {
        queries.clear();
        queries.add(new DefaultQuery<>(generateWord()));
        oracle.processQueries(queries);

        Assert.assertEquals(getNumberOfPosedQueries(), 3);
    }

    @Test(dependsOnMethods = {"testOneNewQuery"})
    public void testPrefix() {
        Assert.assertFalse(queries.isEmpty());

        final Word<I> firstQueryInput = queries.get(0).getInput();
        final Word<I> prefix = firstQueryInput.prefix(firstQueryInput.size() - 1);
        final long oldCount = getNumberOfPosedQueries();

        queries.add(new DefaultQuery<>(prefix));
        oracle.processQueries(queries);

        if (supportsPrefixes()) {
            Assert.assertEquals(getNumberOfPosedQueries(), oldCount);
        } else {
            Assert.assertEquals(getNumberOfPosedQueries(), oldCount + 1);
        }
    }

    @Test(dependsOnMethods = {"testPrefix"})
    public void testCacheConsistency() {

        final EquivalenceOracle<A, I, D> eqOracle = oracle.createCacheConsistencyTest();
        final A target = getTargetModel();
        final A invalidTarget = getInvalidTargetModel();

        final DefaultQuery<I, D> targetCE = eqOracle.findCounterExample(target, getAlphabet());
        final DefaultQuery<I, D> invalidTargetCE = eqOracle.findCounterExample(invalidTarget, getAlphabet());

        Assert.assertNull(targetCE);
        Assert.assertNotNull(invalidTargetCE);

        Assert.assertNotEquals(invalidTarget.computeOutput(invalidTargetCE.getInput()),
                               target.computeOutput(invalidTargetCE.getInput()));
    }

    private Word<I> generateWord() {
        final WordBuilder<I> result = new WordBuilder<>(LENGTH);

        for (int i = 0; i < LENGTH; ++i) {
            int symidx = random.nextInt(alphabet.size());
            I sym = alphabet.getSymbol(symidx);
            result.append(sym);
        }

        return result.toWord();
    }

    protected abstract Alphabet<I> getAlphabet();

    protected abstract A getTargetModel();

    protected abstract A getInvalidTargetModel();

    protected abstract LearningCacheOracle<A, I, D> getCachedOracle();

    protected abstract long getNumberOfPosedQueries();

    protected abstract boolean supportsPrefixes();

}
