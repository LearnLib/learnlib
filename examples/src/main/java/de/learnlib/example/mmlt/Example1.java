/* Copyright (C) 2013-2025 TU Dortmund University
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
package de.learnlib.example.mmlt;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLTBuilder;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;

/**
 * This example shows a basic learning setup for Mealy machine with local timers ({@link MMLT}), an automaton model for
 * real-time systems.
 * <p>
 * <em>Mealy Machines with Local Timers</em> (MMLTs) are an extension of Mealy machines for real-time behavior.
 * They extend Mealy machines with multiple <em>timers</em>. A timer in an MMLT counts down as time progresses. When
 * reaching zero, it stops and triggers an action.</p>
 *
 * <ul>
 *   <li>A timer in an MMLT is bound to a specific location. It can only time out in its associated location and only be reset
 *   at transitions that target this location.</li>
 *   <li>The timeout-action of a timer $x$ is modeled with a transition that uses the internal input {@code to[x]}. These inputs
 *   cannot be provided to the model directly. Instead, they are internally triggered after sufficient time has passed. All
 *   other input symbols are called <em>non-delaying inputs</em>.</li>
 *   <li>The output of a timer at timeout must not be silent.</li>
 *   <li>There are two types of timers:
 *     <ul>
 *       <li>A <em>periodic timer</em> automatically resets itself on timeout. It cannot cause a change to a different location.</li>
 *       <li>A <em>one-shot timer</em> may cause a change to a different location on timeout. Regardless of that, it resets all timers
 *       of the targeted location.</li>
 *     </ul>
 *   </li>
 *   <li>All timers of a location reset to their initial value when this location is entered from a <em>different</em> location. If
 *   the initial location has timers, they are reset when the system is activated.</li>
 *   <li>A self-loop with a non-delaying input does not reset timers by default. However, it might optionally reset all timers
 *   of its source location. This behavior is called a <em>local reset</em>.</li>
 *   <li>A location can have multiple timers:
 *     <ul>
 *       <li>A location can also have multiple periodic timers. These can even time out simultaneously. Then, their outputs are
 *       combined to a single output through concatenation.</li>
 *       <li>A periodic and a one-shot timer must never time out simultaneously.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>As for many other real-time systems, the <em>semantics</em> of an MMLT are defined with an associated transition system.
 * For MMLTs, this system is a Mealy machine. When inferring the behavior of the unknown system, the learner
 * conceptually interacts with this Mealy machine.
 * The inputs of this machine are the non-delaying inputs of the MMLT, discrete time steps,
 * and a symbolic <em>timeout symbol</em>. The latter prompts a delay until the next timeout.</p>
 *
 * <p>More information about MMLTs can be found here:
 * <a href="https://doi.org/10.14279/depositonce-24731">Learning Mealy Machines with Local Timers</a>.</p>
 */
@SuppressWarnings("PMD.UseExplicitTypes") // allow vars in examples
public final class Example1 {

    private Example1() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        // We use the included sensor collector model as reference automaton:
        var model = MMLTExamples.sensorCollector();
        var mmlt = model.getReferenceAutomaton();
        var alphabet = mmlt.getInputAlphabet();

        // We first create a statistics container.
        // This container will store various statistical data during learning:
        var stats = Statistics.getCollector();
        stats.addText("model", null, model.toString());
        stats.setCounter("original_locs", "Locations in original", mmlt.getStates().size());
        stats.setCounter("original_inputs", "Untimed alphabet size in original", alphabet.size());

        // ======================
        // Set up the pipeline:
        // We use a simulator SUL to simulate our automaton:
        var sul = new MMLTSimulatorSUL<>(mmlt);

        // We count all operations that are performed on the SUL with a stats-SUL:
        var statsAfterCache = new CounterTimedSUL<>(sul);

        // We use a cache to avoid redundant operations:
        var cacheSUL = new TimedSULTreeCache<>(statsAfterCache, model.getParams());
        var toReducerSul = new TimeoutReducerSUL<>(cacheSUL, model.getParams().maxTimeoutWaitingTime());

        // We use a query oracle to answer queries from the learner:
        var timeOracle = new TimedSULOracle<>(toReducerSul, model.getParams());

        // In the basic set-up, we use a simulator oracle to answer equivalence queries.
        // This oracle has perfect knowledge of the reference automaton.
        var eqOracle = new SimulatorEQOracle<>(mmlt);

        // Set up our L* learner:

        // We provide the learner with an initial set of suffixes.
        // We include all untimed inputs and the symbolic timeout symbol, which causes the learner to wait
        // until the next timeout (but no longer than model.getParams().maxTimeoutWaitingTime()).
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        alphabet.forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        var learner = new ExtensibleLStarMMLTBuilder<String, String>().withAlphabet(alphabet)
                                                                      .withModelParams(model.getParams())
                                                                      .withTimeOracle(timeOracle)
                                                                      .withInitialSuffixes(suffixes)
                                                                      .create();

        // Start learning:
        ExampleRunner.runExperiment(learner, eqOracle, mmlt.getSemantics().getInputAlphabet(), stats);
    }

}
