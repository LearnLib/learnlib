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
package de.learnlib.examples.example1;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Symbol;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.eqtests.basic.WMethodEQOracle;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.experiments.Experiment;
import de.learnlib.oracles.CounterOracle;
import de.learnlib.oracles.SimulatorOracle;
import de.learnlib.statistics.SimpleProfiler;

/**
 * This example shows the usage of a learning algorithm and an equivalence test
 * as part of an experiment in order to learn a simulated SUL (system under
 * learning).
 *
 * @author falkhowar
 */
public class Example {

    public static void main(String[] args) throws IOException {

        // load DFA and alphabet
        // the ? leaves open the state implementation since we do not
        // need to know it explicitly
        DFA<?, Symbol>  target = ExampleAngluin.constructMachine();
        Alphabet<Symbol> inputs = ExampleAngluin.getAlphabet();

        // typed empty word
        Word<Symbol> epsilon = Word.epsilon();

        // construct a simulator membership query oracle
        // input  - Symbol  (determined by example)
        // output - Boolean (determined by DFA)
        SimulatorOracle<Symbol, Boolean> sul = new SimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        CounterOracle<Symbol, Boolean> mqOracle =
                new CounterOracle<>(sul, "membership queries");

        // construct L* instance
        ExtensibleLStarDFA<Symbol> lstar = new ExtensibleLStarDFA<>(
                inputs,                                     // input alphabet
                mqOracle,                                   // mq oracle
                Collections.singletonList(epsilon),         // initial suffixes
                ObservationTableCEXHandlers.CLASSIC_LSTAR,  // handling of counterexamples
                ClosingStrategies.CLOSE_FIRST               // always choose first unclosedness found
                );

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        WMethodEQOracle<DFA<?, Symbol>, Symbol, Boolean> wMethod =
                new WMethodEQOracle<>(4, mqOracle);

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        Experiment<DFA<?, Symbol>> experiment =
                new Experiment<>(lstar, wMethod, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        DFA<?, Symbol> result = experiment.getFinalHypothesis();

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
