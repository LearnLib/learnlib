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

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.equivalence.KWayStateCoverEQOracleBuilder;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import de.learnlib.query.DefaultQuery;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.Automata;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

/**
 * An example of constructing a learn-loop using reactive streams (from RXJava) to compute counterexamples.
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.UseExplicitTypes"}) // allow println and vars in examples
public final class RXJavaExample {

    private static final int SEED = 42;
    private static final int SIZE = 10;
    private static final int NUM_INPUTS = 4;
    private static final int RND_LENGTH = 4;
    private static final int LIMIT = 1000;

    private RXJavaExample() {
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
        var tracker = new ProgressTracker<>(learner, mqo);

        // setup scheduler
        // IMPORTANT: set interruptibleWorker to false in order to allow graceful oracle/SUL shutdown
        var pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        var scheduler = Schedulers.from(pool, false, true);

        // learning loop
        learner.startLearning();
        var hyp = learner.getHypothesisModel();

        while (!tracker.hasFinished()) {
            // since we only access the hypothesis in read-only fashion it is fine to share the reference across threads
            final var finalHyp = hyp;
            Flowable // create a publisher from first quivalence oracle
                     .fromStream(eqo.generateTestWords(finalHyp, inputs))
                     // limit to 1000 elements
                     .take(LIMIT)
                     // merge with elements from second equivalence oracle
                     .mergeWith(Flowable.fromStream(eqo2.generateTestWords(finalHyp, inputs)))
                     // process items in parallel
                     .parallel()
                     // run on our non-interruptible scheduler
                     .runOn(scheduler)
                     // filter for counterexamples
                     .filter(w -> !Objects.equals(mqo.answerQuery(w), finalHyp.computeOutput(w)))
                     // process them sequentially
                     .sequential()
                     // the first counterexample suffices for refinement
                     .firstElement()
                     // use a blocking subscribe to ensure that all pipelines are cleared for the next iteration
                     // alternatively, use blockingGet like in the ReactorExample
                     .blockingSubscribe(tracker, System.out::println, tracker);
        }

        // cleanup
        pool.shutdown();
        mqo.shutdown();
        scheduler.shutdown();

        // process results
        hyp = learner.getHypothesisModel();

        System.out.println("Final hypothesis size " + hyp.size());
        System.out.println("Is equivalent? " + Automata.testEquivalence(mealy, hyp, inputs));
    }

    private static final class ProgressTracker<I, D> implements Consumer<Word<I>>, Action {

        private boolean finished;
        private final LearningAlgorithm<?, I, D> learner;
        private final MembershipOracle<I, D> oracle;

        private ProgressTracker(LearningAlgorithm<?, I, D> learner, MembershipOracle<I, D> oracle) {
            this.learner = learner;
            this.oracle = oracle;
        }

        /**
         * The onSuccess handler. Upon receiving a counterexample, refine the hypothesis.
         *
         * @param w
         *         the counterexample
         */
        @Override
        public void accept(Word<I> w) {
            learner.refineHypothesis(new DefaultQuery<>(w, oracle.answerQuery(w)));
        }

        /**
         * The onComplete handler. When all items have been processed without finding a counterexample, terminate the
         * learning loop.
         */
        @Override
        public void run() {
            this.finished = true;
        }

        private boolean hasFinished() {
            return finished;
        }
    }
}
