/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.examples.example2;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.cache.sul.SULCaches;
import de.learnlib.drivers.reflect.AbstractMethodInput;
import de.learnlib.drivers.reflect.AbstractMethodOutput;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticSUL;

/**
 * This example shows how a model of a Java class can be learned using the SUL
 * (system under learning) interfaces and the random walks equivalence test.
 *
 * @author falkhowar
 */
public class Example {

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
        private Deque<String> data = new ArrayDeque<>(3);

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
    
    
    public static void main(String[] args) throws NoSuchMethodException, IOException {

        // instantiate test driver
        SimplePOJOTestDriver driver = new SimplePOJOTestDriver(
                BoundedStringQueue.class.getConstructor());
                
        // create learning alphabet
        Method mOffer = BoundedStringQueue.class.getMethod(
                "offer", new Class<?>[]{String.class});
        Method mPoll = BoundedStringQueue.class.getMethod(
                "poll", new Class<?>[]{});
                
        // offer
        AbstractMethodInput offer_a = driver.addInput("offer_a", mOffer, "a");
        AbstractMethodInput offer_b = driver.addInput("offer_b", mOffer, "b");

        // poll
        AbstractMethodInput poll = driver.addInput("poll", mPoll);

        // oracle for counting queries wraps sul
        StatisticSUL<AbstractMethodInput, AbstractMethodOutput> statisticSul = 
                new ResetCounterSUL<>("membership queries", driver);
        
        SUL<AbstractMethodInput, AbstractMethodOutput> effectiveSul = statisticSul;
        // use caching in order to avoid duplicate queries
        effectiveSul = SULCaches.createCache(driver.getInputs(), effectiveSul);
        
        SULOracle<AbstractMethodInput, AbstractMethodOutput> mqOracle = new SULOracle<>(effectiveSul);

        // create initial set of suffixes
        List<Word<AbstractMethodInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(offer_a));
        suffixes.add(Word.fromSymbols(offer_b));
        suffixes.add(Word.fromSymbols(poll));

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table 
        // instead of single outputs.
        MealyLearner<AbstractMethodInput, AbstractMethodOutput> lstar
        	= new ExtensibleLStarMealyBuilder<AbstractMethodInput,AbstractMethodOutput>()
        		.withAlphabet(driver.getInputs()) // input alphabet
        		.withOracle(mqOracle)			  // membership oracle
        		.create();
                

        // create random walks equivalence test
        MealyEquivalenceOracle<AbstractMethodInput, AbstractMethodOutput> randomWalks =
                new RandomWalkEQOracle<>(
                0.05, // reset SUL w/ this probability before a step 
                10000, // max steps (overall)
                false, // reset step count after counterexample 
                new Random(46346293), // make results reproducible 
                driver // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<AbstractMethodInput, AbstractMethodOutput> experiment =
                new MealyExperiment<>(lstar, randomWalks, driver.getInputs());

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, AbstractMethodInput, ?, AbstractMethodOutput> result = 
                experiment.getFinalHypothesis();

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
        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(result, driver.getInputs(), w);
        w.close();

        System.out.println("-------------------------------------------------------");

    }
}
