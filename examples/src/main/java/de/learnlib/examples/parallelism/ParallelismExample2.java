/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.examples.parallelism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.parallelism.ParallelOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.cache.sul.SULCache;
import de.learnlib.filter.cache.sul.ThreadSafeSULCaches;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.random.RandomUtil;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * An example for using caches in combination with {@link ParallelOracle}s.
 *
 * @author frohme
 */
@SuppressWarnings("PMD.SystemPrintln")
public class ParallelismExample2 {

    private static final int AUTOMATON_SIZE = 100;
    private static final int NUM_QUERIES = 10_000;
    private static final int MAX_QUERY_LENGTH = 25;
    private static final int BATCH_SIZE = 50;

    private final Alphabet<Integer> alphabet;
    private final SUL<Integer, Character> automatonAsSUL;
    private final int numInstances;

    private final Collection<DefaultQuery<Integer, Word<Character>>> queries;

    public ParallelismExample2() {
        final Alphabet<Integer> inputs = Alphabets.integers(0, 9);
        final Alphabet<Character> outputs = Alphabets.characters('a', 'z');
        final CompactMealy<Integer, Character> automaton =
                RandomAutomata.randomMealy(new Random(0), AUTOMATON_SIZE, inputs, outputs);

        this.alphabet = inputs;
        this.automatonAsSUL = new MealySimulatorSUL<>(automaton);

        // we want to limit the number of threads to the number of available cores but at least spawn two threads
        this.numInstances = Math.max(2, Runtime.getRuntime().availableProcessors());

        System.out.println("Generating queries");

        final Random random = new Random(69);
        final List<Integer> sampleList = new ArrayList<>(inputs);
        this.queries = new ArrayList<>(NUM_QUERIES);

        for (int i = 0; i < NUM_QUERIES; i++) {
            queries.add(new DefaultQuery<>(Word.fromList(RandomUtil.sample(sampleList,
                                                                           random.nextInt(MAX_QUERY_LENGTH),
                                                                           random))));
        }
    }

    public static void main(String[] args) {
        ParallelismExample2 example = new ParallelismExample2();

        example.runSingleCache();
        example.runThreadSafeCache();
    }

    /**
     * Creates a setup that is based on the pipeline <i>consumer</i> -&gt; <i>cache</i> -&gt; <i>(parallel) oracles</i>.
     * In this case, the cache is queried first and all cache-misses are delegated to the parallel oracles at once,
     * without sharing any information during (parallel) processing. On the other hand, the cache may perform better
     * optimizations (e.g. re-ordering queries to exploit prefix-closedness) due to having all queries available.
     */
    private void runSingleCache() {
        // create a counter to count the effective number of queries executed on the SUL
        final ResetCounterSUL<Integer, Character> counter = new ResetCounterSUL<>("#Queries", automatonAsSUL);

        // create the parallel oracle
        // use a dynamic oracle because a static oracle splits queries into static chunks that will be delegated to the
        // worker-threads evenly, thus not allowing to show differences in the cache behavior
        final ParallelOracle<Integer, Word<Character>> parallelOracle =
                ParallelOracleBuilders.newDynamicParallelOracle(counter)
                                      .withPoolSize(numInstances)
                                      .withBatchSize(BATCH_SIZE)
                                      .create();

        // create a single-threaded cache that delegates cache misses to the parallel oracle
        final MealyMembershipOracle<Integer, Character> cache = MealyCaches.createCache(alphabet, parallelOracle);

        // print results
        System.out.println("Single-threaded cache performance: ");
        answerQueries(cache);
        System.out.println("  " + counter.getStatisticalData().getSummary());

        parallelOracle.shutdownNow();
    }

    /**
     * Creates a setup that is based on the pipeline <i>consumer</i> -&gt; <i>parallel oracle</i> -&gt; <i>individual
     * oracles with a shared cache</i>. In this case, the parallel oracle delegates the queries to its worker threads
     * first and the respective worker-threads share information with each other via a shared cache. While this
     * potentially improves query performance due to fewer duplicate queries (as already answered queries can be
     * directly seen by other worker-threads), the optimization potential of the cache may be lower because only small
     * chunks of queries can be analyzed at each step and the cache requires additional synchronization overhead.
     */
    private void runThreadSafeCache() {
        // create a counter to count the effective number of queries executed on the SUL
        final ResetCounterSUL<Integer, Character> counter = new ResetCounterSUL<>("#Queries", automatonAsSUL);

        // create a thread-safe cache for the SUL
        final SULCache<Integer, Character> cache = ThreadSafeSULCaches.createCache(alphabet, counter);

        // create the parallel oracle that now also forks the cache
        // use a dynamic oracle because a static oracle splits queries into static chunks that will be delegated to the
        // worker-threads evenly, thus not allowing to show differences in the cache behavior
        final ParallelOracle<Integer, Word<Character>> parallelOracle =
                ParallelOracleBuilders.newDynamicParallelOracle(cache)
                                      .withPoolSize(numInstances)
                                      .withBatchSize(BATCH_SIZE)
                                      .create();

        // print results
        System.out.println("Shared cache performance: ");
        answerQueries(parallelOracle);
        System.out.println("  " + counter.getStatisticalData().getSummary());

        parallelOracle.shutdownNow();
    }

    private void answerQueries(MembershipOracle<Integer, Word<Character>> oracle) {
        long t0 = System.currentTimeMillis();
        oracle.processQueries(queries);
        long t1 = System.currentTimeMillis();

        System.out.println("  Answering queries took " + (t1 - t0) + "ms");
    }

}
