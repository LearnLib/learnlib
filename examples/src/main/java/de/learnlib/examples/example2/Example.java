/* Copyright (C) 2013 TU Dortmund
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.SUL;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment;
import de.learnlib.oracles.CounterOracle;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;

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

    /*
     * We use the BSQInput class to wrap concrete method invocations
     * and use these as alphabet symbols for the learning algorithm
     */
    public static class BSQInput {

        // method to invoke
        public final Method action;
        
        // method parameter values
        public final Object[] data;

        public BSQInput(Method action, Object[] data) {
            this.action = action;
            this.data = data;
        }

        // this will be used for printing when 
        // logging or exporting automata
        @Override
        public String toString() {
            return action.getName() + Arrays.toString(data);
        }
    }

    /*
     * The BSQAdapter 
     * 
     */
    public static class BSQAdapter implements SUL<BSQInput, String> {

        // system under learning
        private BoundedStringQueue sul;

        // reset the SUL
        @Override
        public void reset() {
            // we just create a new instance
            sul = new BoundedStringQueue();
        }

        // execute one input on the SUL
        @Override
        public String step(BSQInput in) {
            try {
                // invoke the method wrapped by in
                Object ret = in.action.invoke(sul, in.data);
                // make sure that we return a string
                return ret == null ? "" : (String) ret;
            } 
            catch (IllegalAccessException | 
                    IllegalArgumentException | 
                    InvocationTargetException e) {
                // This should never happen. In a real experiment
                // this would be the point when we want to issue
                // a warning or stop the learning.
                return "err";
            }
        }
    }

    public static void main(String[] args) throws NoSuchMethodException, IOException {

        // create learning alphabet
        
        // offer(a)
        BSQInput offer_a = new BSQInput(BoundedStringQueue.class.getMethod(
                "offer", new Class<?>[]{String.class}), new Object[]{"a"});

        // offer(b)
        BSQInput offer_b = new BSQInput(BoundedStringQueue.class.getMethod(
                "offer", new Class<?>[]{String.class}), new Object[]{"b"});

        // poll()
        BSQInput poll = new BSQInput(BoundedStringQueue.class.getMethod(
                "poll", new Class<?>[]{}), new Object[]{});

        Alphabet<BSQInput> inputs = new SimpleAlphabet<>();
        inputs.add(offer_a);
        inputs.add(offer_b);
        inputs.add(poll);

        // create an oracle that can answer membership queries
        // using the BSQAdapter
        SUL<BSQInput, String> sul = new BSQAdapter();
        SULOracle<BSQInput, String> backOracle = new SULOracle<>(sul);

        // oracle for counting queries wraps sul
        CounterOracle<BSQInput, Word<String>> mqOracle =
                new CounterOracle<>(backOracle, "membership queries");

        // create initial set of suffixes
        List<Word<BSQInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(offer_a));
        suffixes.add(Word.fromSymbols(offer_b));
        suffixes.add(Word.fromSymbols(poll));

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table 
        // instead of single outputs.
        LearningAlgorithm<? extends MealyMachine<?, BSQInput, ?, String>, BSQInput, Word<String>> lstar =
                new ExtensibleLStarMealy<>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                suffixes, // initial suffixes
                ObservationTableCEXHandlers.CLASSIC_LSTAR, // handling of counterexamples
                ClosingStrategies.CLOSE_FIRST // always choose first unclosedness found 
                );

        // create random walks equivalence test
        EquivalenceOracle<MealyMachine<?, BSQInput, ?, String>, BSQInput, Word<String>> randomWalks =
                new RandomWalkEQOracle<>(
                0.05, // reset SUL w/ this probability before a step 
                10000, // max steps (overall)
                false, // reset step count after counterexample 
                new Random(46346293), // make results reproducible 
                sul // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        Experiment<MealyMachine<?,BSQInput,?,String>> experiment =
                new Experiment<>(lstar, randomWalks, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, BSQInput, ?, String> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(mqOracle.getCounter().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        System.out.println();
        System.out.println("Model: ");
        
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(result, inputs, w);
        w.close();

        System.out.println("-------------------------------------------------------");

    }
}
