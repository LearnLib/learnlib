/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.example.reactive;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;

import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.equivalence.KWayStateCoverEQOracleBuilder;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.query.DefaultQuery;
import io.smallrye.mutiny.Multi;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

/**
 * An example of constructing a learn-loop using reactive streams (from SmallRye) to compute counterexamples.
 */
// allow println and vars in examples, ExecutorService does not implement AutoClosable until Java 19+
@SuppressWarnings("PMD")
public final class MutinyExample {

    private static final int SEED = 42;
    private static final int SIZE = 10;
    private static final int NUM_INPUTS = 4;
    private static final int RND_LENGTH = 4;
    private static final int LIMIT = 1000;

    private MutinyExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        // setup symbols
        var inputs = Alphabets.integers(0, NUM_INPUTS);
        var outputs = Alphabets.characters('a', 'd');

        // setup membership oracle
        var mealy = RandomAutomata.randomMealy(new Random(SEED), SIZE, inputs, outputs);
        var sul = new MealySimulatorSUL<>(mealy);
        // IMPORTANT: make sure to use parallel-aware oracle (with thread local instances)
        // because it will be called from different threads in the reactive environment
        var mqo = ParallelOracleBuilders.newDynamicParallelOracle(sul).create();

        // setup equivalence oracles
        var eqo = new RandomWMethodEQOracle<>(mqo, SIZE / 2, RND_LENGTH);
        var eqo2 =
                new KWayStateCoverEQOracleBuilder<MealyMachine<?, Integer, ?, Character>, Integer, Word<Character>>().withOracle(
                        mqo).withRandom(new Random(SEED)).create();

        // setup learner
        var learner = new TTTLearnerMealy<>(inputs, mqo);

        // setup thread pools
        var pool = Executors.newFixedThreadPool(1);
        var pool2 = Executors.newFixedThreadPool(1);
        var pool3 = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // learning loop
        learner.startLearning();
        var hyp = learner.getHypothesisModel();

        while (true) {
            // since we only access the hypothesis in read-only fashion it is fine to share the reference across threads
            final var finalHyp = hyp;
            var m1 = Multi.createFrom()
                          // create a (cold) publisher from first quivalence oracle
                          .items(() -> eqo.generateTestWords(finalHyp, inputs))
                          // sample on own thread
                          .runSubscriptionOn(pool)
                          // limit to 1000 elements
                          .select().first(LIMIT);
            var m2 = Multi.createFrom()
                          // create a (cold) publisher from second quivalence oracle
                          .items(() -> eqo2.generateTestWords(finalHyp, inputs))
                          // sample on own thread
                          .runSubscriptionOn(pool2);

            var ce = Multi.createBy()
                          // merge elements from both oracles in interleaving fashion
                          .merging().streams(m1, m2)
                          // run processing in parallel
                          .runSubscriptionOn(pool3)
                          // filter for counterexamples
                          .filter(w -> !Objects.equals(mqo.answerQuery(w), finalHyp.computeOutput(w)))
                          // toUni implicitly fetches the first element
                          .toUni()
                          // use a blocking call to extract the final counterexample
                          .await().indefinitely();

            if (ce != null) {
                learner.refineHypothesis(new DefaultQuery<>(ce, mqo.answerQuery(ce)));
                hyp = learner.getHypothesisModel();
            } else {
                break;
            }
        }

        // cleanup
        mqo.shutdown();
        pool.shutdown();
        pool2.shutdown();
        pool3.shutdown();

        // process results
        hyp = learner.getHypothesisModel();

        System.out.println("Final hypothesis size " + hyp.size());
        System.out.println("Is equivalent? " + Automata.testEquivalence(mealy, hyp, inputs));
    }
}
