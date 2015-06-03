/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.examples.example1;

import java.io.IOException;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
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

    	// create automaton
    	CompactDFA<Character> dfa = AutomatonBuilders.newDFA(sigma)
    			.from("q0")
    				.on('a').to("q1")
    				.on('b').to("q2")
    			.from("q1")
    				.on('a').to("q0")
    				.on('b').to("q3")
    			.from("q2")
    				.on('a').to("q3")
    				.on('b').to("q0")
    			.from("q3")
    				.on('a').to("q2")
    				.on('b').to("q1")
    			.withInitial("q0")
    			.withAccepting("q0")
    			.create();

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

        Visualization.visualizeAutomaton(result, inputs, true);

        System.out.println("-------------------------------------------------------");
        
        System.out.println("Final observation table:");
        new ObservationTableASCIIWriter<>().write(lstar.getObservationTable(), System.out);
        
        OTUtils.displayHTMLInBrowser(lstar.getObservationTable());
    }
}
