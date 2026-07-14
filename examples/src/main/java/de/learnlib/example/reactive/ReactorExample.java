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

import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.equivalence.KWayStateCoverEQOracleBuilder;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.query.DefaultQuery;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * An example of constructing a learn-loop using reactive streams (from Project Reactor) to compute counterexamples.
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.UseExplicitTypes"}) // allow println and vars in examples
public final class ReactorExample {

    private static final int SEED = 42;
    private static final int SIZE = 10;
    private static final int BATCH_SIZE = 10;
    private static final int NUM_INPUTS = 4;
    private static final int RND_LENGTH = 4;
    private static final int LIMIT = 1000;

    private ReactorExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        // setup symbols
        var inputs = Alphabets.integers(0, NUM_INPUTS);
        var outputs = Alphabets.characters('a', 'd');

        // setup membership oracle
        var mealy = RandomAutomata.randomMealy(new Random(SEED), SIZE, inputs, outputs);
        var sul = new MealySimulatorSUL<>(mealy);
        // note that we can still use a parallel oracle to answer query batches in parallel
        var mqo = ParallelOracleBuilders.newStaticParallelOracle(sul)
                                        .withNumInstances(BATCH_SIZE)
                                        .withMinBatchSize(1)
                                        .create();

        // setup equivalence oracles
        var eqo = new RandomWMethodEQOracle<>(mqo, SIZE / 2, RND_LENGTH);
        var eqo2 =
                new KWayStateCoverEQOracleBuilder<MealyMachine<?, Integer, ?, Character>, Integer, Word<Character>>().withOracle(
                        mqo).withRandom(new Random(SEED)).create();

        // setup learner
        var learner = new TTTLearnerMealy<>(inputs, mqo);

        // learning loop
        learner.startLearning();
        var hyp = learner.getHypothesisModel();

        while (true) {
            final var finalHyp = hyp;
            var ce = Flux
                    // create a (cold) publisher from first quivalence oracle
                    .fromStream(() -> eqo.generateTestWords(finalHyp, inputs))
                    // sample on own thread
                    .subscribeOn(Schedulers.boundedElastic())
                    // limit to 1000 elements
                    .take(LIMIT)
                    // merge with elements from second equivalence oracle, also sampled in its own thread
                    .mergeWith(Flux.fromStream(() -> eqo2.generateTestWords(finalHyp, inputs))
                                   .subscribeOn(Schedulers.boundedElastic()))
                    // map to queries ...
                    .map(DefaultQuery<Integer, Word<Character>>::new)
                    // ... create batches ...
                    .buffer(BATCH_SIZE)
                    // ... and process in bulk
                    // NOTE: this happens synchronously to allow the oracle/SUL to gracefully shutdown
                    // I have not found a way to tell Reactor to not forcefully interrupt canceled threads ...
                    // However, the oracle can still answer the batch in parallel itself
                    .doOnNext(mqo::processQueries)
                    // flat to individual queries
                    .flatMapIterable(l -> l)
                    // filter for counterexamples
                    .filter(q -> !Objects.equals(q.getOutput(), finalHyp.computeSuffixOutput(q.getPrefix(), q.getSuffix())))
                    // use a blocking call to extract the final counterexample
                    .blockFirst();

            if (ce != null) {
                learner.refineHypothesis(ce);
                hyp = learner.getHypothesisModel();
            } else {
                break;
            }
        }

        // cleanup
        mqo.shutdown();

        // process results
        hyp = learner.getHypothesisModel();

        System.out.println("Final hypothesis size " + hyp.size());
        System.out.println("Is equivalent? " + Automata.testEquivalence(mealy, hyp, inputs));
    }
}
