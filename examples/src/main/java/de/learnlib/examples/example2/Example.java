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
package de.learnlib.examples.example2;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.drivers.reflect.AbstractMethodOutput;
import de.learnlib.drivers.reflect.MethodInput;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.Experiment.MealyExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Word;

/**
 * This example shows how a model of a Java class can be learned using the SUL (system under learning) interfaces and
 * the random walks equivalence test.
 *
 * @author falkhowar
 */
public final class Example {

    private static final double RESET_PROBABILITY = 0.05;
    private static final int MAX_STEPS = 10000;
    private static final int RANDOM_SEED = 46346293;

    private Example() {
        // prevent instantiation
    }

    public static void main(String[] args) throws NoSuchMethodException, IOException {

        // instantiate test driver
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(BoundedStringQueue.class.getConstructor());

        // create learning alphabet
        Method mOffer = BoundedStringQueue.class.getMethod("offer", String.class);
        Method mPoll = BoundedStringQueue.class.getMethod("poll");

        // offer
        MethodInput offerA = driver.addInput("offer_a", mOffer, "a");
        MethodInput offerB = driver.addInput("offer_b", mOffer, "b");

        // poll
        MethodInput poll = driver.addInput("poll", mPoll);

        // oracle for counting queries wraps sul
        StatisticSUL<MethodInput, AbstractMethodOutput> statisticSul =
                new ResetCounterSUL<>("membership queries", driver);

        SUL<MethodInput, AbstractMethodOutput> effectiveSul = statisticSul;
        // use caching in order to avoid duplicate queries
        effectiveSul = SULCaches.createCache(driver.getInputs(), effectiveSul);

        SULOracle<MethodInput, AbstractMethodOutput> mqOracle = new SULOracle<>(effectiveSul);

        // create initial set of suffixes
        List<Word<MethodInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(offerA));
        suffixes.add(Word.fromSymbols(offerB));
        suffixes.add(Word.fromSymbols(poll));

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table
        // instead of single outputs.
        MealyLearner<MethodInput, AbstractMethodOutput> lstar =
                new ExtensibleLStarMealyBuilder<MethodInput, AbstractMethodOutput>().withAlphabet(driver.getInputs()) // input alphabet
                                                                                    .withOracle(mqOracle) // membership oracle
                                                                                    .withInitialSuffixes(suffixes) // initial suffixes
                                                                                    .create();

        // create random walks equivalence test
        MealyEquivalenceOracle<MethodInput, AbstractMethodOutput> randomWalks =
                new RandomWalkEQOracle<>(driver, // system under learning
                                         RESET_PROBABILITY, // reset SUL w/ this probability before a step
                                         MAX_STEPS, // max steps (overall)
                                         false, // reset step count after counterexample
                                         new Random(RANDOM_SEED) // make results reproducible
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<MethodInput, AbstractMethodOutput> experiment =
                new MealyExperiment<>(lstar, randomWalks, driver.getInputs());

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, MethodInput, ?, AbstractMethodOutput> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(statisticSul.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + driver.getInputs().size());

        // show model
        System.out.println();
        System.out.println("Model: ");

        GraphDOT.write(result, driver.getInputs(), System.out); // may throw IOException!
        Visualization.visualize(result, driver.getInputs());

        System.out.println("-------------------------------------------------------");

    }

    /*
     * The BoundedStringQueue is the class of which we are going to
     * infer a model. It wraps an ordinary queue of Strings, limiting
     * its size to MAX_SIZE (3). Once the queue is full, additional
     * offers will be ignored.
     * <p>
     * However, the implementation uses the underlying queue in a strange
     * way as the model will reveal.
     */
    public static class BoundedStringQueue {

        // capacity
        public static final int MAX_SIZE = 3;
        // storage
        private final Deque<String> data = new ArrayDeque<>(3);

        // add a String to the queue if capacity allows
        public void offer(String s) {
            if (data.size() < MAX_SIZE) {
                data.offerFirst(s);
            }
        }

        // get next element from queue (null for empty queue)
        public String poll() {
            return data.poll();
        }
    }
}
