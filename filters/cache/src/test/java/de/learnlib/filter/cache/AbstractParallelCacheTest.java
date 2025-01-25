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
import java.util.List;
import java.util.stream.IntStream;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.ParallelOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.word.Word;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A test for checking proper synchronization of the different cache implementations.
 * <p>
 * Normally, the oracle chain would look like <i>learner</i> -> <i>cache</i> -> <i>parallel oracles</i> so that you have
 * a single(-threaded) shared cache, whose cache-misses would be answered by oracles in parallel.
 * <p>
 * This test checks that the other way around, i.e. <i>learner</i> -> <i>(parallel querying) cache</i> -> <i>single
 * oracle</i> does at least not throw any synchronization errors.
 */
public abstract class AbstractParallelCacheTest<A, I, D> {

    private static final int MAXIMUM_LENGTH_OF_QUERIES = 5;

    private Alphabet<I> alphabet;
    private A targetModel;
    private LearningCache<A, I, D> cache;
    private ParallelOracle<I, D> parallelOracle;

    protected abstract Alphabet<I> getAlphabet();

    protected abstract A getTargetModel();

    protected abstract LearningCache<A, I, D> getCacheRepresentative();

    protected abstract ParallelOracle<I, D> getParallelOracle();

    protected abstract long getNumberOfQueries();

    @BeforeClass
    public void setUp() {
        this.alphabet = getAlphabet();
        this.targetModel = getTargetModel();
        this.cache = getCacheRepresentative();
        this.parallelOracle = getParallelOracle();
    }

    @AfterClass
    public void teardown() {
        this.parallelOracle.shutdownNow();
    }

    @Test(timeOut = 20000)
    public void testConcurrentMembershipQueries() {
        Assert.assertEquals(getNumberOfQueries(), 0);

        final int numQueries = (int) IntStream.rangeClosed(0, MAXIMUM_LENGTH_OF_QUERIES)
                                              .mapToDouble(i -> Math.pow(alphabet.size(), i))
                                              .sum();

        final List<CountingQuery<I, D>> queries = new ArrayList<>(numQueries);

        for (List<I> word : IterableUtil.allTuples(alphabet, 0, MAXIMUM_LENGTH_OF_QUERIES)) {
            queries.add(new CountingQuery<>(Word.fromList(word)));
        }

        this.parallelOracle.processQueries(queries);
        final long numOfQueriesBefore = getNumberOfQueries();
        final long numAnsweredBefore = queries.stream().mapToInt(CountingQuery::getNumAnswered).sum();

        Assert.assertEquals(numAnsweredBefore, numQueries);
        // Since e.g. Mealy Machines are prefix closed, a single cache hit can answer all its prefixes.
        // This we cannot enforce equality here
        Assert.assertTrue(numOfQueriesBefore <= numAnsweredBefore);

        this.parallelOracle.processQueries(queries);

        final long numOfQueriesAfter = getNumberOfQueries();
        final long numAnsweredAfter = queries.stream().mapToInt(CountingQuery::getNumAnswered).sum();

        Assert.assertEquals(numAnsweredAfter, 2 * numAnsweredBefore);
        Assert.assertEquals(numOfQueriesAfter, numOfQueriesBefore);
    }

    @Test(dependsOnMethods = "testConcurrentMembershipQueries", timeOut = 20000)
    public void testConcurrentEquivalenceQueries() {
        final long previousCount = getNumberOfQueries();
        final EquivalenceOracle<? super A, I, D> eqOracle = cache.createCacheConsistencyTest();

        final List<DefaultQuery<I, D>> queries =
                new ArrayList<>((int) Math.pow(alphabet.size(), MAXIMUM_LENGTH_OF_QUERIES));

        for (List<I> word : IterableUtil.allTuples(alphabet,
                                                   MAXIMUM_LENGTH_OF_QUERIES + 1,
                                                   MAXIMUM_LENGTH_OF_QUERIES + 1)) {
            queries.add(new DefaultQuery<>(Word.fromList(word)));
        }

        Thread task = new Thread(() -> {
            while (true) {
                if (eqOracle.findCounterExample(targetModel, alphabet) != null) {
                    throw new IllegalStateException();
                } else {
                    try {
                        // do not constantly block the cache, otherwise the test takes to long
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // if we get interrupted, just terminate
                        return;
                    }
                }
            }
        });

        task.start();
        this.parallelOracle.processQueries(queries);
        task.interrupt();

        final long numOfQueries = getNumberOfQueries();
        Assert.assertEquals(numOfQueries, queries.size() + previousCount);
    }

    private static class CountingQuery<I, D> extends DefaultQuery<I, D> {

        private int numAnswered;

        CountingQuery(Word<I> input) {
            super(input);
        }

        @Override
        public void answer(D output) {
            numAnswered++;
            super.answer(output);
        }

        public int getNumAnswered() {
            return numAnswered;
        }
    }
}
