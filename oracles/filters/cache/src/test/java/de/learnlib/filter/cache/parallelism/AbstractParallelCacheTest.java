/* Copyright (C) 2013-2019 TU Dortmund
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.LearningCacheOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.oracle.parallelism.ParallelOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.util.MQUtil;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.Assert;
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
 *
 * @author frohme
 */
public abstract class AbstractParallelCacheTest<A extends SuffixOutput<I, D>, I, D> {

    protected static final int MODEL_SIZE = 10;
    private static final int NUMBER_OF_THREADS = 10;
    private static final int BATCH_SIZE = 10;
    private static final int MAXIMUM_LENGTH_OF_QUERIES = 5;

    private CounterOracle<I, D> counterOracle;
    private Alphabet<I> alphabet;
    private ParallelOracle<I, D> parallelOracle;
    private LearningCacheOracle<A, I, D> cache;
    private A targetModel;

    protected abstract Alphabet<I> getAlphabet();

    protected abstract A getTargetModel();

    protected abstract LearningCacheOracle<A, I, D> getCache(Alphabet<I> alphabet, MembershipOracle<I, D> oracle);

    @BeforeClass
    public void setUp() {
        this.alphabet = getAlphabet();
        this.targetModel = getTargetModel();

        this.counterOracle = new CounterOracle<>(new SimulatorOracle<>(targetModel), "Queries");
        this.cache = getCache(this.alphabet, this.counterOracle);
    }

    @Test(timeOut = 20000)
    public void testConcurrentMembershipQueriesImplicit() {
        testConcurrentMembershipQueries(queries -> MQUtil.answerQueriesParallel(cache, queries));
    }

    @Test(dependsOnMethods = "testConcurrentMembershipQueriesImplicit", timeOut = 20000)
    public void testConcurrentEquivalenceQueriesImplicit() {
        testConcurrentEquivalenceQueries(queries -> MQUtil.answerQueriesParallel(cache, queries));
    }

    @Test(dependsOnMethods = "testConcurrentEquivalenceQueriesImplicit", timeOut = 20000)
    public void testConcurrentMembershipQueriesExplicit() {
        setUp();
        this.parallelOracle = ParallelOracleBuilders.newDynamicParallelOracle(() -> cache)
                                                    .withPoolSize(NUMBER_OF_THREADS)
                                                    .withBatchSize(BATCH_SIZE)
                                                    .create();

        testConcurrentMembershipQueries(parallelOracle::processQueries);
    }

    @Test(dependsOnMethods = "testConcurrentMembershipQueriesExplicit", timeOut = 20000)
    public void testConcurrentEquivalenceQueriesExplicit() {
        testConcurrentEquivalenceQueries(parallelOracle::processQueries);
        parallelOracle.shutdownNow();
    }

    private void testConcurrentMembershipQueries(Consumer<? super Collection<? extends Query<I, D>>> mqConsumer) {

        final List<DefaultQuery<I, D>> queries =
                new ArrayList<>(((int) Math.pow(alphabet.size(), MAXIMUM_LENGTH_OF_QUERIES)) + 1);

        for (final List<I> word : CollectionsUtil.allTuples(alphabet, 0, MAXIMUM_LENGTH_OF_QUERIES)) {
            queries.add(new DefaultQuery<>(Word.fromList(word)));
        }

        mqConsumer.accept(queries);
        final long numOfQueriesBefore = counterOracle.getCount();

        queries.forEach(q -> q.answer(null));

        mqConsumer.accept(queries);
        final long numOfQueriesAfter = counterOracle.getCount();

        Assert.assertEquals(numOfQueriesAfter, numOfQueriesBefore);
    }

    private void testConcurrentEquivalenceQueries(Consumer<? super Collection<? extends Query<I, D>>> mqConsumer) {

        final long previousCount = counterOracle.getCount();
        final EquivalenceOracle<A, I, D> eqOracle = cache.createCacheConsistencyTest();

        final List<DefaultQuery<I, D>> queries = new ArrayList<>(
                (int) Math.pow(alphabet.size(), MAXIMUM_LENGTH_OF_QUERIES + 1) -
                (int) Math.pow(alphabet.size(), MAXIMUM_LENGTH_OF_QUERIES));

        for (final List<I> word : CollectionsUtil.allTuples(alphabet,
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
        mqConsumer.accept(queries);
        task.interrupt();

        final long numOfQueries = counterOracle.getCount();
        Assert.assertEquals(numOfQueries, queries.size() + previousCount);
    }
}
