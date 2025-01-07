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
package de.learnlib.filter.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.learnlib.Resumable;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.query.Query;
import de.learnlib.testsupport.ResumeUtils;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.concept.Output;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A simple test against various cache implementations.
 */
public abstract class AbstractCacheTest<OR extends LearningCacheOracle<A, I, D>, A extends Output<I, D>, I, D> {

    protected static final int LENGTH = 5;
    private final Random random = new Random(42);
    private Alphabet<I> alphabet;
    protected OR oracle;
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

    @Test(dependsOnMethods = "testNoQueriesReceived")
    public void testFirstQuery() {
        queries.add(new DefaultQuery<>(generateWord()));

        Assert.assertEquals(queries.size(), 1);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 1);
    }

    @Test(dependsOnMethods = "testFirstQuery")
    public void testFirstDuplicate() {
        Assert.assertEquals(queries.size(), 1);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 1);
    }

    @Test(dependsOnMethods = "testFirstDuplicate")
    public void testTwoQueriesOneDuplicate() {
        queries.add(new DefaultQuery<>(generateWord()));
        Assert.assertEquals(queries.size(), 2);
        oracle.processQueries(queries);
        Assert.assertEquals(getNumberOfPosedQueries(), 2);
    }

    @Test(dependsOnMethods = "testTwoQueriesOneDuplicate")
    public void testOneNewQuery() {
        queries.clear();
        queries.add(new DefaultQuery<>(generateWord()));
        oracle.processQueries(queries);

        Assert.assertEquals(getNumberOfPosedQueries(), 3);
    }

    @Test(dependsOnMethods = "testOneNewQuery")
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

    @Test(dependsOnMethods = "testPrefix")
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

    @Test(dependsOnMethods = "testCacheConsistency")
    public void testResuming() {

        final OR resumedOracle = getResumedOracle(oracle);
        final long oldCount = getNumberOfPosedQueries();

        resumedOracle.processQueries(queries);

        // resumed oracle should retain cache information
        Assert.assertEquals(getNumberOfPosedQueries(), oldCount);

        resumedOracle.answerQuery(generateWord());

        // but also be able to answer new queries
        // note, however, if we use mapping, it may happen, that a query is not posed, even though the cache does not
        // have sufficient data on the word. so this check may not be applicable
        if (!usesMapping()) {
            Assert.assertEquals(getNumberOfPosedQueries(), oldCount + 1);
        }
    }

    @Test(dependsOnMethods = "testResuming")
    public void testAddSymbol() {
        if (supportsGrowing()) {
            @SuppressWarnings("unchecked")
            final SupportsGrowingAlphabet<I> growingCache = (SupportsGrowingAlphabet<I>) oracle;

            // test that adding existing symbols does nothing;
            final long oldCount = getNumberOfPosedQueries();
            alphabet.forEach(growingCache::addAlphabetSymbol);
            final long newCount = getNumberOfPosedQueries();
            Assert.assertEquals(newCount, oldCount);

            // test that adding new symbols does nothing;
            final Alphabet<I> extensions = getExtensionAlphabet();
            final long oldCount2 = getNumberOfPosedQueries();
            extensions.forEach(growingCache::addAlphabetSymbol);
            final long newCount2 = getNumberOfPosedQueries();
            Assert.assertEquals(newCount2, oldCount2);

            // test that adding new queries works
            queries.clear();
            queries.add(new DefaultQuery<>(generateWord()));
            final long oldCount3 = getNumberOfPosedQueries();
            oracle.processQueries(queries);
            final long newCount3 = getNumberOfPosedQueries();
            Assert.assertEquals(newCount3, oldCount3 + 1);

            // test that querying new queries works
            final long oldCount4 = getNumberOfPosedQueries();
            oracle.processQueries(queries);
            final long newCount4 = getNumberOfPosedQueries();
            Assert.assertEquals(newCount4, oldCount4);
        }
    }

    @Test(dependsOnMethods = "testAddSymbol")
    public void testDuplicatesInBatch() {

        final long oldCount = getNumberOfPosedQueries();
        final Word<I> word = generateWord();
        final int halfLength = LENGTH / 2;

        // Generate a query which essentially queries the same information but is split across prefix and suffix and
        // a simple duplicate
        final DefaultQuery<I, D> q1 = new DefaultQuery<>(word);
        final DefaultQuery<I, D> q2 = new DefaultQuery<>(word.prefix(halfLength), word.suffix(-halfLength));
        final DefaultQuery<I, D> q3 = new DefaultQuery<>(word);

        this.oracle.processQueries(Arrays.asList(q1, q2, q3));

        // assert queries are answered
        Assert.assertNotNull(q1.getOutput());
        Assert.assertNotNull(q2.getOutput());
        Assert.assertNotNull(q3.getOutput());

        // assert that the three queries only asked the system once
        Assert.assertEquals(getNumberOfPosedQueries(), oldCount + 1);
    }

    private Word<I> generateWord() {
        return generateWord(alphabet);
    }

    private Word<I> generateWord(Alphabet<I> alphabet) {
        final WordBuilder<I> result = new WordBuilder<>(LENGTH);

        for (int i = 0; i < LENGTH; ++i) {
            int symidx = random.nextInt(alphabet.size());
            I sym = alphabet.getSymbol(symidx);
            result.append(sym);
        }

        return result.toWord();
    }

    protected static <T> void serializeResumable(Resumable<T> source, Resumable<T> target) {
        byte[] bytes = ResumeUtils.toBytes(source.suspend());
        target.resume(ResumeUtils.fromBytes(bytes));
    }

    protected boolean usesMapping() {
        return false;
    }

    protected Query<I, D> getQuery(int i) {
        return queries.get(i);
    }

    protected abstract Alphabet<I> getAlphabet();

    protected abstract Alphabet<I> getExtensionAlphabet();

    protected abstract A getTargetModel();

    protected abstract A getInvalidTargetModel();

    protected abstract OR getCachedOracle();

    protected abstract OR getResumedOracle(OR original);

    protected abstract long getNumberOfPosedQueries();

    protected abstract boolean supportsPrefixes();

    protected abstract boolean supportsGrowing();

}
