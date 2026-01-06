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
package de.learnlib.example.mmlt;

import java.io.IOException;
import java.io.InputStream;
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
import de.learnlib.time.MMLTModelParams;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.impl.StringSymbolCombiner;
import net.automatalib.exception.FormatException;
import net.automatalib.serialization.dot.DOTMMLTParser;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.util.automaton.mmlt.MMLTs;
import net.automatalib.word.Word;

/**
 * This example demonstrates how to load an {@link MMLT} from a dot-file and learn it using the L* algorithm.
 * <p>
 * A description of the dot-file syntax for MMLTs can be found in AutomataLib (see {@link DOTMMLTParser}).
 */
@SuppressWarnings("PMD.UseExplicitTypes") // allow vars in examples
public final class Example4 {

    private Example4() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        // First, we load the file "mmlt_example.dot" from the "resources" folder:
        MMLT<?, String, ?, String> mmlt;
        Alphabet<String> alphabet;
        MMLTModelParams<String> params;

        // We define the output that represents silence:
        var silentOutput = "void";

        // In an MMLT, a timeout may yield multiple outputs.
        // We use a symbol combiner to combine them into a single output.
        // Here, we use a StringSymbolCombiner that sorts symbols and then concatenates them with a pipe:
        var outputCombiner = StringSymbolCombiner.getInstance();
        var parser = DOTParsers.mmlt(silentOutput, outputCombiner);

        try (InputStream is = Example4.class.getResourceAsStream("/mmlt_example.dot")) {
            var parsedModel = parser.readModel(is);
            mmlt = parsedModel.model;
            alphabet = parsedModel.alphabet;

            // During learning, we use a symbolic "timeout" symbol to indicate that the
            // teacher should wait for the next timeout. To avoid an infinite runtime,
            // we set a maximum waiting time for these symbols.
            // This time should be at least the maximum time to the next timeout in any
            // state of the system. We configure this as follows:
            long maxTimeoutDelay = MMLTs.getMaximumTimeoutDelay(mmlt);

            // After adding a new location, the learner infers timers for it by watching the SUL for timeouts.
            // To learn an accurate model, the maximum time to watch for these timeouts must be at
            // least the value of "maxTimeoutDelay".
            // If the maximum initial value of timers in the SUL is known or can be reasonably estimated,
            // setting the watch time to twice that value usually yields good results:
            long maxTimerQueryWaitingFinal = MMLTs.getMaximumInitialTimerValue(mmlt) * 2;

            params = new MMLTModelParams<>(silentOutput, outputCombiner, maxTimeoutDelay, maxTimerQueryWaitingFinal);
        } catch (IOException | FormatException e) {
            throw new IllegalStateException("Unable to load model from file.", e);
        }

        // Proceed as in Example1:

        var statistics = Statistics.getService();
        statistics.setText(Example1.KEY_MODEL, "mmlt_example.dot");
        statistics.setCounter(Example1.KEY_LOCS, mmlt.getStates().size());
        statistics.setCounter(Example1.KEY_SYMS, alphabet.size());

        // Set up the pipeline:
        // We use a simulator SUL to simulate our automaton:
        var sul = new MMLTSimulatorSUL<>(mmlt);

        // We count all operations that are performed on the SUL with a stats-SUL:
        var statsAfterCache = new CounterTimedSUL<>(sul, "post-cache");

        // We use a cache to avoid redundant operations:
        var cacheSUL = new TimedSULTreeCache<>(statsAfterCache, params);
        var toReducerSul = new TimeoutReducerSUL<>(cacheSUL, params.maxTimeoutWaitingTime());
        var statsBeforeCache = new CounterTimedSUL<>(toReducerSul, "pre-cache");

        // We use a query oracle to answer queries from the learner:
        var timeOracle = new TimedSULOracle<>(statsBeforeCache, params);

        // In the basic set-up, we use a simulator oracle to answer equivalence queries.
        // This oracle has perfect knowledge of the reference automaton.
        var eqOracle = new SimulatorEQOracle<>(mmlt);

        // Set up our L* learner:

        // We provide the learner with an initial set of suffixes.
        // We include all untimed inputs and the symbolic timeout symbol, which causes the learner to wait
        // until the next timeout (but no longer than params.maxTimeoutWaitingTime()).
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        alphabet.forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        var learner = new ExtensibleLStarMMLTBuilder<String, String>().withAlphabet(alphabet)
                                                                      .withModelParams(params)
                                                                      .withTimeOracle(timeOracle)
                                                                      .withInitialSuffixes(suffixes)
                                                                      .create();

        // Start learning:
        ExampleRunner.runExperiment(learner, eqOracle, mmlt.getSemantics().getInputAlphabet());
    }

}
