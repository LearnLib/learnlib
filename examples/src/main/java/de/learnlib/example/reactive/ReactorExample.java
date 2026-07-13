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
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * An example of constructing a learn-loop using reactive streams (from Project Reactor) to compute counterexamples.
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.UseExplicitTypes"}) // allow println and vars in examples
public final class ReactorExample {

    private static final int SEED = 42;
    private static final int SIZE = 10;
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

        // setup scheduler
        // IMPORTANT: set interruptibleWorker to false in order to allow graceful oracle/SUL shutdown
        var pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//        var scheduler = new PureNonInterruptingScheduler(Schedulers.parallel());
        // Decorate all BoundedElastic schedulers (or any standard pools)
        Schedulers.addExecutorServiceDecorator("prevent-interrupts", (sched, executorService) ->
                new ScheduledThreadPoolExecutor(1) { // Or wrap the existing executorService
                    @Override
                    protected <V> RunnableScheduledFuture<V> decorateTask(
                            Runnable runnable, RunnableScheduledFuture<V> task) {
                        return new CustomScheduledFuture<>(task);
                    }
                }
        );

        // learning loop
        learner.startLearning();
        var hyp = learner.getHypothesisModel();

        while (true) {
            final var finalHyp = hyp;
            var ce = Flux.fromStream(eqo.generateTestWords(finalHyp, inputs))
                         .take(LIMIT)
                         .mergeWith(Flux.fromStream(eqo2.generateTestWords(finalHyp, inputs)))
                         .parallel()
                         .runOn(Schedulers.parallel())
                         .filter(w -> !Objects.equals(mqo.answerQuery(w), finalHyp.computeOutput(w)))
                         .sequential()
                         .next()
                         .block();

            if (ce != null) {
                learner.refineHypothesis(new DefaultQuery<>(ce, mqo.answerQuery(ce)));
                hyp = learner.getHypothesisModel();
            } else {
                break;
            }
        }

        // cleanup
        pool.shutdown();
        mqo.shutdown();
//        scheduler.dispose();

        // process results
        hyp = learner.getHypothesisModel();

        System.out.println("Final hypothesis size " + hyp.size());
        System.out.println("Is equivalent? " + Automata.testEquivalence(mealy, hyp, inputs));
    }

    private static class NonInterruptingScheduler implements Scheduler {

        private final Scheduler delegate;

        public NonInterruptingScheduler(Scheduler delegate) {
            this.delegate = delegate;
        }

        @Override
        public Disposable schedule(Runnable task) {
            // Wrap the task to clear or ignore the interrupted status if something upstream triggers it
            Runnable safeTask = () -> {
                try {
                    task.run();
                } catch (Exception e) {
                    // Handle or catch unexpected InterruptedExceptions
                } finally {
                    // Clear the interrupted status flag just in case
                    Thread.interrupted();
                }
            };
            return delegate.schedule(safeTask);
        }

        @Override
        public Worker createWorker() {
            return delegate.createWorker();
        }

        @Override
        public void dispose() {
            delegate.dispose();
        }
    }

    public static class PureNonInterruptingScheduler implements Scheduler {

        private final Scheduler delegate;
        private final ExecutorService executor = Executors.newFixedThreadPool(10);

        public PureNonInterruptingScheduler(Scheduler delegate) {
            this.delegate = delegate;
        }

        @Override
        public Disposable schedule(Runnable task) {
            return delegate.schedule(task);
        }

        @Override
        public Worker createWorker() {
            return new NonInterruptingWorker(executor);
        }

        @Override
        public void dispose() {
            executor.shutdown();
            delegate.dispose();
        }

        private static class NonInterruptingWorker implements Worker {

            private final ExecutorService executor;

            public NonInterruptingWorker(ExecutorService executor) {
                this.executor = executor;
            }

            @Override
            public Disposable schedule(Runnable task) {
                Future<?> future = executor.submit(task);
                return () -> future.cancel(false); // Overrides thread interruption logic
            }

            @Override
            public void dispose() {
                // Keep empty if you do not want worker disposal to break the underlying pool
            }
        }
    }
    // A delegate wrapper that ignores the "mayInterruptIfRunning" flag
    private static class CustomScheduledFuture<V> implements RunnableScheduledFuture<V> {
        private final RunnableScheduledFuture<V> delegate;

        public CustomScheduledFuture(RunnableScheduledFuture<V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // FORCE false to prevent Thread.interrupt()
            return delegate.cancel(false);
        }

        // Delegate all other standard interface methods...
        @Override public boolean isCancelled() { return delegate.isCancelled(); }
        @Override public boolean isDone() { return delegate.isDone(); }
        @Override public V get() throws InterruptedException, ExecutionException { return delegate.get(); }
        @Override public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
                                                                   TimeoutException { return delegate.get(timeout, unit); }
        @Override public long getDelay(TimeUnit unit) { return delegate.getDelay(unit); }
        @Override public int compareTo(Delayed o) { return delegate.compareTo(o); }
        @Override public void run() { delegate.run(); }
        @Override public boolean isPeriodic() { return delegate.isPeriodic(); }
    }
}
