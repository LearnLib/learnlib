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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLTBuilder;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTRandomSymbolFilter;
import de.learnlib.algorithm.lstar.mmlt.filter.MMLTStatisticsSymbolFilter;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.filter.symbol.CachedSymbolFilter;
import de.learnlib.oracle.equivalence.MMLTEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.RandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchEQOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;

/**
 * This example illustrates how to learn {@link MMLT}s with symbol filtering.
 * <p>
 * A symbol filter is a component that provides information about transitions that might silently self-loop. The learner
 * exploits this information to avoid redundant queries on the SUL. The symbol filter might incorrectly classify a
 * transition as silent self-loop. The MMLT-learner detects and corrects such errors.
 * <p>
 * LearnLib includes four types of symbol filter:
 * <ul>
 * <li> AcceptAllSymbolFilter: no transition is considered as silent self-loop.
 * This is the default behavior if no filter is provided. </li>
 * <li> PerfectSymbolFilter: simulates perfect knowledge of silent self-loops.
 * Perfect knowledge is useful for benchmarking but rarely the case in practice. </li>
 * <li> IgnoreAllSymbolFilter: considers all transitions to be silent self-loops.
 * If no knowledge of the SUL is available, this filter still often yields strong performance improvements.</li>
 * <li>RandomSymbolFilter: simulates incorrect responses with a certain percentage.</li>
 * </ul>
 * <p>
 * When you apply MMLT-learning in practice, you usually want to implement your own symbol filter that exploits specific
 * domain knowledge.
 */
@SuppressWarnings({"checkstyle:magicnumber", "PMD.UseExplicitTypes"}) // allow magic numbers and vars in examples
public final class Example3 {

    private static final int BOUND = 100;
    private static final double INACC_PROB = 0.1;
    private static final int MIN_SIZE = 16;
    private static final double PERCENTAGE = 1.0;
    private static final int SEED = 100;

    private Example3() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        var model = MMLTExamples.sensorCollector();
        var mmlt = model.getReferenceAutomaton();
        var alphabet = mmlt.getInputAlphabet();

        // We first create a statistics container.
        // This container will store various statistical data during learning:
        var statistics = Statistics.getService();
        statistics.setText(Example1.KEY_MODEL, model.toString());
        statistics.setCounter(Example1.KEY_LOCS, mmlt.getStates().size());
        statistics.setCounter(Example1.KEY_SYMS, alphabet.size());

        // ======================
        // Set up the pipeline:
        // We use a simulator SUL to simulate our automaton:
        var sul = new MMLTSimulatorSUL<>(mmlt);

        // We count all operations that are performed on the SUL with a stats-SUL:
        var statsAfterCache = new CounterTimedSUL<>(sul, "post-cache");

        // We use a cache to avoid redundant operations:
        var cacheSUL = new TimedSULTreeCache<>(statsAfterCache, model.getParams());
        var toReducerSul = new TimeoutReducerSUL<>(cacheSUL, model.getParams().maxTimeoutWaitingTime());
        var statsBeforeCache = new CounterTimedSUL<>(toReducerSul, "pre-cache");

        // We use a query oracle to answer queries from the learner:
        var timeOracle = new TimedSULOracle<>(statsBeforeCache, model.getParams());

        // We use a chain of different equivalence oracles (see Example2):
        MMLTEQOracleChain<String, String> chainOracle = new MMLTEQOracleChain<>();
        chainOracle.addOracle(new CounterEQOracle<>(cacheSUL.createCacheConsistencyTest(), "cache"));
        chainOracle.addOracle(new CounterEQOracle<>(new ResetSearchEQOracle<>(timeOracle, SEED, PERCENTAGE, PERCENTAGE),
                                                    "reset"));
        chainOracle.addOracle(new CounterEQOracle<>(new RandomWpMethodEQOracle<>(timeOracle, SEED, MIN_SIZE, 0, BOUND),
                                                    "wp"));

        // Set up our L* learner:
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        alphabet.forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        // A symbol filter allows us to reduce queries by exploiting prior knowledge.
        // For this example, we use a AbstractRandomSymbolFilter. This filter correctly predicts
        // whether a transition silently self-loops with an accuracy of 90%:
        SymbolFilter<TimedInput<String>, InputSymbol<String>> filter =
                new MMLTRandomSymbolFilter<>(mmlt, INACC_PROB, new Random(SEED));

        // We wrap our filter with a StatisticsFilter to collect useful statistics about the filter:
        filter = new MMLTStatisticsSymbolFilter<>(mmlt, filter);

        // The learner may need to update incorrect responses of the filter.
        // To facilitate this, we wrap our filter with a CachedFilter:
        var cachedFilter = new CachedSymbolFilter<>(filter);

        var learner = new ExtensibleLStarMMLTBuilder<String, String>().withAlphabet(alphabet)
                                                                      .withModelParams(model.getParams())
                                                                      .withTimeOracle(timeOracle)
                                                                      .withInitialSuffixes(suffixes)
                                                                      .withSymbolFilter(cachedFilter)
                                                                      .create();

        // Start learning:
        var finalModel = ExampleRunner.runExperiment(learner, chainOracle, mmlt.getSemantics().getInputAlphabet());

        // In this set-up, we actually know the reference automaton.
        // This allows us to check that we learned an accurate model:
        var simOracle = new SimulatorEQOracle<>(mmlt);
        if (simOracle.findCounterExample(finalModel, finalModel.getSemantics().getInputAlphabet()) != null) {
            throw new IllegalStateException("Incorrect model learned.");
        }
    }

}
