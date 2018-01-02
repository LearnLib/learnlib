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
package de.learnlib.examples.example3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.examples.example2.Example.BoundedStringQueue;
import de.learnlib.filter.reuse.ReuseCapableOracle;
import de.learnlib.filter.reuse.ReuseOracle;
import de.learnlib.filter.reuse.ReuseOracle.ReuseOracleBuilder;
import de.learnlib.filter.reuse.tree.SystemStateHandler;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.SimpleAlphabet;

/**
 * This example shows how to use the reuse filter on the {@link BoundedStringQueue} of {@link
 * de.learnlib.examples.example2.Example}.
 * <p>
 * Please note that there is no equivalence oracle used in this example so the resulting mealy machines are only first
 * "guesses".
 *
 * @author Oliver Bauer
 */
public class Example {

    private static final String OFFER_1 = "offer_1";
    private static final String OFFER_2 = "offer_2";
    private static final String POLL = "poll";
    private final Alphabet<String> sigma;
    private final List<Word<String>> initialSuffixes;

    public Example() {
        sigma = new SimpleAlphabet<>();
        sigma.add(OFFER_1);
        sigma.add(OFFER_2);
        sigma.add(POLL);

        initialSuffixes = new ArrayList<>();
        for (String symbol : sigma) {
            initialSuffixes.add(Word.fromLetter(symbol));
        }
    }

    public static void main(String[] args) {
        Example example = new Example();
        System.out.println("--");
        System.out.println("Run experiment 1 (ReuseOracle):");
        MealyMachine<?, String, ?, String> result1 = example.runExperiment1();
        System.out.println("--");
        System.out.println("Run experiment 2:");
        MealyMachine<?, String, ?, String> result2 = example.runExperiment2();
        System.out.println("--");
        System.out.println("Model 1: " + result1.size() + " states");
        System.out.println("Model 2: " + result2.size() + " states");

        Word<String> sepWord;
        sepWord = Automata.findSeparatingWord(result1, result2, example.sigma);

        System.out.println("Difference (separating word)? " + sepWord);

        /*
         * Background knowledge: L^* creates an observation table with
         * |S|=3,|SA|=7,|E|=3 so 30 MQs should be sufficient. The reuse filter
         * should sink system states from S*E to SA*E so the upper part is the
         * number of saved resets (=9).
         *
         * If the numbers don't match:
         * https://github.com/LearnLib/learnlib/issues/5 (9 queries will be
         * pumped by the reuse filter)
         */
    }

    /*
     * A "normal" scenario without reuse filter technique.
     */
    public MealyMachine<?, String, ?, String> runExperiment1() {
        // For each membership query a new instance of BoundedStringQueue will
        // be created (normal learning scenario without filters)
        FullMembershipQueryOracle oracle = new FullMembershipQueryOracle();

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table
        // instead of single outputs.
        MealyLearner<String, String> lstar;
        lstar = new ExtensibleLStarMealyBuilder<String, String>().withAlphabet(sigma)
                                                                 .withInitialSuffixes(initialSuffixes)
                                                                 .withOracle(oracle)
                                                                 .create();

        lstar.startLearning();
        MealyMachine<?, String, ?, String> result;
        result = lstar.getHypothesisModel();

        System.out.println("Resets:  " + oracle.resets);
        System.out.println("Symbols: " + oracle.symbols);

        return result;
    }

    /*
     * Scenario with reuse filter technique.
     */
    public MealyMachine<?, String, ?, String> runExperiment2() {
        MySystemStateHandler ssh = new MySystemStateHandler();

        // This time we use the reuse filter to avoid some resets and
        // save execution of symbols
        ReuseOracle<BoundedStringQueue, String, String> reuseOracle =
                new ReuseOracleBuilder<>(sigma, ReuseCapableImpl::new).withSystemStateHandler(ssh).build();

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table
        // instead of single outputs.

        MealyLearner<String, String> lstar;
        lstar = new ExtensibleLStarMealyBuilder<String, String>().withAlphabet(sigma)
                                                                 .withInitialSuffixes(initialSuffixes)
                                                                 .withOracle(reuseOracle)
                                                                 .create();

        lstar.startLearning();

        // get learned model
        MealyMachine<?, String, ?, String> result = lstar.getHypothesisModel();

        // now invalidate all system states and count the number of disposed
        // queues (equals number of resets)
        reuseOracle.getReuseTree().disposeSystemstates();
        ReuseCapableImpl reuseCapableOracle = (ReuseCapableImpl) reuseOracle.getReuseCapableOracle();
        System.out.println("Resets:   " + reuseCapableOracle.fullQueries);
        System.out.println("Reused:   " + reuseCapableOracle.reused);
        System.out.println("Symbols:  " + reuseCapableOracle.symbols);
        // disposed = resets
        System.out.println("Disposed: " + ssh.disposed);

        return result;
    }

    private String exec(BoundedStringQueue s, String input) {
        switch (input) {
            case OFFER_1:
            case OFFER_2:
                s.offer(input);
                return "void";
            case POLL:
                return s.poll();
            default:
                throw new IllegalArgumentException("unknown input symbol");
        }
    }

    /**
     * For running the example this class could also be removed/ignored. It is only here for documentation purposes.
     */
    static class MySystemStateHandler implements SystemStateHandler<BoundedStringQueue> {

        private int disposed;

        @Override
        public void dispose(BoundedStringQueue state) {
            /*
             * When learning e.g. examples that involve databases, here would be
             * a good point to remove database entities. In this example we just
             * count the number of disposed (garbage collection inside the reuse
             * tree) objects.
             */
            disposed++;
        }
    }

    /**
     * An oracle that also does the reset by creating a new instance of the {@link BoundedStringQueue}.
     */
    class FullMembershipQueryOracle implements MealyMembershipOracle<String, String> {

        private int resets;
        private int symbols;

        @Override
        public void processQueries(Collection<? extends Query<String, Word<String>>> queries) {
            for (Query<String, Word<String>> query : queries) {
                resets++;
                symbols += query.getInput().size();

                BoundedStringQueue s = new BoundedStringQueue();

                WordBuilder<String> output = new WordBuilder<>();
                for (String input : query.getInput()) {
                    output.add(exec(s, input));
                }

                query.answer(output.toWord().suffix(query.getSuffix().size()));
            }
        }
    }

    /**
     * An implementation of the {@link ReuseCapableOracle} needed for the {@link ReuseOracle}. It only does resets (by
     * means of creating a new {@link BoundedStringQueue} instance in {@link ReuseCapableOracle#processQuery(Word)}.
     */
    class ReuseCapableImpl implements ReuseCapableOracle<BoundedStringQueue, String, String> {

        private int reused;
        private int fullQueries;
        private int symbols;

        @Override
        public QueryResult<BoundedStringQueue, String> continueQuery(Word<String> trace, BoundedStringQueue s) {

            reused++;
            symbols += trace.size();

            WordBuilder<String> output = new WordBuilder<>();

            for (String input : trace) {
                output.add(exec(s, input));
            }

            QueryResult<BoundedStringQueue, String> result;
            result = new QueryResult<>(output.toWord(), s);

            return result;
        }

        @Override
        public QueryResult<BoundedStringQueue, String> processQuery(Word<String> trace) {
            fullQueries++;
            symbols += trace.size();

            // Suppose the reset would be a time consuming operation
            BoundedStringQueue s = new BoundedStringQueue();

            WordBuilder<String> output = new WordBuilder<>();

            for (String input : trace) {
                output.add(exec(s, input));
            }

            QueryResult<BoundedStringQueue, String> result;
            result = new QueryResult<>(output.toWord(), s);

            return result;
        }
    }
}
