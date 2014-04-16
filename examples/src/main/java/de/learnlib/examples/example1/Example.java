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

import de.learnlib.algorithms.features.observationtable.OTUtils;
import de.learnlib.algorithms.features.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFABuilder;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.eqtests.basic.WMethodEQOracle.DFAWMethodEQOracle;
import de.learnlib.experiments.Experiment.DFAExperiment;
import de.learnlib.oracles.CounterOracle.DFACounterOracle;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.statistics.SimpleProfiler;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * This example shows the usage of a learning algorithm and an equivalence test
 * as part of an experiment in order to learn a simulated SUL (system under
 * learning).
 *
 * @author falkhowar
 */
public class Example {

    /**
     * creates example from Angluin's seminal paper.
     * 
     * @return example dfa
     */
    private static CompactDFA<Character> constructSUL() {
        // input alphabet contains characters 'a'..'b'
    	Alphabet<Character> sigma = Alphabets.characters('a', 'b');

        // create states
        CompactDFA<Character> dfa = new CompactDFA<>(sigma);
        int q0 = dfa.addInitialState(true);
        int q1 = dfa.addState(false);
        int q2 = dfa.addState(false);
        int q3 = dfa.addState(false);

        // create transitions
        dfa.addTransition(q0, 'a', q1);
        dfa.addTransition(q0, 'b', q2);
        dfa.addTransition(q1, 'a', q0);
        dfa.addTransition(q1, 'b', q3);
        dfa.addTransition(q2, 'a', q3);
        dfa.addTransition(q2, 'b', q0);
        dfa.addTransition(q3, 'a', q2);
        dfa.addTransition(q3, 'b', q1);

        return dfa;
    }

    public static void main(String[] args) throws IOException {

        // load DFA and alphabet
        CompactDFA<Character> target = constructSUL();
        Alphabet<Character> inputs = target.getInputAlphabet();

        // construct a simulator membership query oracle
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> sul = new DFASimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracle =
                new DFACounterOracle<>(sul, "membership queries");

        
        // construct L* instance
        ExtensibleLStarDFA<Character> lstar = new ExtensibleLStarDFABuilder<Character>()
        		.withAlphabet(inputs) // input alphabet
        		.withOracle(mqOracle) // membership oracle
        		.create();
        

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        DFAWMethodEQOracle<Character> wMethod =
                new DFAWMethodEQOracle<>(4, mqOracle);

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        DFAExperiment<Character> experiment =
                new DFAExperiment<>(lstar, wMethod, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);


        // run experiment
        experiment.run();

        // get learned model
        DFA<?, Character> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(mqOracle.getStatisticalData().getSummary());

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
        
        System.out.println("Final observation table:");
        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);
        
        OTUtils.displayHTMLInBrowser(lstar.getObservationTable());
    }
}
