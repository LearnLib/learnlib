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
package de.learnlib.example.parallelism;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.ParallelOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.collection.IterableUtil;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

/**
 * An example for creating {@link ParallelOracle}s using the {@link ParallelOracleBuilders} factory.
 */
@SuppressWarnings("PMD.SystemPrintln")
public class ParallelismExample1 {

    private static final int AUTOMATON_SIZE = 100;
    private static final int QUERIES_EXP = 6;

    private final CompactMealy<Integer, Character> automaton;
    private final SUL<Integer, Character> automatonAsSUL;

    private final Collection<DefaultQuery<Integer, Word<Character>>> queries;

    public ParallelismExample1() {
        final Alphabet<Integer> inputs = Alphabets.integers(0, 9);
        final Alphabet<Character> outputs = Alphabets.characters('a', 'z');
        this.automaton = RandomAutomata.randomMealy(new Random(0), AUTOMATON_SIZE, inputs, outputs);
        this.automatonAsSUL = new MealySimulatorSUL<>(automaton);

        System.out.println("Generating queries");

        // generate 1 million (10^6) input words
        this.queries = new ArrayList<>((int) Math.pow(inputs.size(), QUERIES_EXP));
        for (List<Integer> input : IterableUtil.allTuples(inputs, QUERIES_EXP)) {
            queries.add(new DefaultQuery<>(Word.fromList(input)));
        }
    }

    public static void main(String[] args) {
        ParallelismExample1 example = new ParallelismExample1();

        example.runSequentialOracles();
        example.runParallelOracles();
    }

    private void runSequentialOracles() {
        final MealyMembershipOracle<Integer, Character> sequentialSULOracle = new SULOracle<>(automatonAsSUL);
        final MealySimulatorOracle<Integer, Character> sequentialMQOracle = new MealySimulatorOracle<>(automaton);

        System.out.println("Sequential SUL oracle");
        answerQueries(sequentialSULOracle);

        System.out.println("Sequential MQ oracle");
        answerQueries(sequentialMQOracle);
    }

    private void runParallelOracles() {

        // we want to limit the number of parallel threads to the number of available cores
        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        final ParallelOracle<Integer, Word<Character>> parallelSULOracle =
                ParallelOracleBuilders.newStaticParallelOracle(this.automatonAsSUL)
                                      .withNumInstances(availableProcessors)
                                      .create();

        final MealySimulatorOracle<Integer, Character> oracle = new MealySimulatorOracle<>(this.automaton);
        // MealySimulatorOracle is thread-safe
        final ParallelOracle<Integer, Word<Character>> parallelMQOracle =
                ParallelOracleBuilders.newStaticParallelOracle(() -> oracle)
                                      .withNumInstances(availableProcessors)
                                      .create();

        System.out.println("Parallel SUL oracle");
        answerQueries(parallelSULOracle);

        System.out.println("Parallel MQ oracle");
        answerQueries(parallelMQOracle);

        parallelSULOracle.shutdownNow();
        parallelMQOracle.shutdownNow();
    }

    private void answerQueries(MembershipOracle<Integer, Word<Character>> oracle) {
        long t0 = System.currentTimeMillis();
        oracle.processQueries(queries);
        long t1 = System.currentTimeMillis();

        System.out.println("  Answering queries took " + (t1 - t0) + "ms");
    }

}
