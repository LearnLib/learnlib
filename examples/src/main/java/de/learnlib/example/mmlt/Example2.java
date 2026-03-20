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

import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLTBuilder;
import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.filter.cache.mmlt.TimeoutReducerSUL;
import de.learnlib.filter.statistic.oracle.CounterEQOracle;
import de.learnlib.filter.statistic.sul.CounterTimedSUL;
import de.learnlib.oracle.equivalence.MMLTEQOracleChain;
import de.learnlib.oracle.equivalence.mmlt.RandomWpMethodEQOracle;
import de.learnlib.oracle.equivalence.mmlt.ResetSearchEQOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.statistic.Statistics;
import de.learnlib.testsupport.example.mmlt.MMLTExamples;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;

/**
 * This example shows a basic set-up of the MMLT-learner for a black-box setting.
 * <p>
 * For this, we use a chain of different equivalence oracles that can be applied if the reference automaton is not
 * known.
 */
@SuppressWarnings("PMD.UseExplicitTypes") // allow magic numbers and vars in examples
public final class Example2 {

    private static final int BOUND = 100;
    private static final int MIN_SIZE = 16;
    private static final double PERCENTAGE = 1.0;
    private static final int SEED = 100;

    private Example2() {
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

        // We use a chain of different equivalence oracles to find counterexamples more efficiently:
        var chainOracle = new MMLTEQOracleChain<String, String>();
        // A cache oracle tests if the current hypothesis and the reference automaton give the same outputs
        // for all words that have already been queried. As the words have already been queried, this
        // executes no additional queries on the SUL:
        chainOracle.addOracle(new CounterEQOracle<>(cacheSUL.createCacheConsistencyTest(), "cache"));

        // A ResetSearchOracle tests for missing local resets, which often require many and/or long test words
        // when using random-based testing. We configure the tester to consider all transitions that might cause a reset:
        chainOracle.addOracle(new CounterEQOracle<>(new ResetSearchEQOracle<>(timeOracle, SEED, PERCENTAGE, PERCENTAGE),
                                                    "reset"));

        // Finally, we add an MMLT-specific RandomWp oracle:
        chainOracle.addOracle(new CounterEQOracle<>(new RandomWpMethodEQOracle<>(timeOracle, SEED, MIN_SIZE, 0, BOUND),
                                                    "wp"));

        // Set up our L* learner:
        List<Word<TimedInput<String>>> suffixes = new ArrayList<>();
        alphabet.forEach(s -> suffixes.add(Word.fromLetter(TimedInput.input(s))));
        suffixes.add(Word.fromLetter(new TimeoutSymbol<>()));

        var learner = new ExtensibleLStarMMLTBuilder<String, String>().withAlphabet(alphabet)
                                                                      .withModelParams(model.getParams())
                                                                      .withTimeOracle(timeOracle)
                                                                      .withInitialSuffixes(suffixes)
                                                                      .create();

        // Start learning:
        var finalModel = ExampleRunner.runExperiment(learner, chainOracle, mmlt.getSemantics().getInputAlphabet());

        // In this set-up, we actually know the reference automaton.
        // This allows us to check that we learned an accurate model:
        var simOracle = new SimulatorEQOracle<>(mmlt);
        if (simOracle.findCounterExample(finalModel, finalModel.getSemantics().getInputAlphabet()) != null) {
            throw new IllegalStateException("Incorrect model learned.");
        }

        // Troubleshooting
        // If you attempt to learn a model of some application and the learner
        // throws assertion errors or illegal state exceptions,
        // your SUL likely has no MMLT semantics.
        // In this case, you can try to learn a partial model by excluding TimeStepSymbol
        // from the input alphabet for the counterexample search:
        // In your learn-loop (see Experiment), replace
        // equivalenceAlgorithm.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet());
        // with: equivalenceAlgorithm.findCounterExample(hyp, hyp.getSemantics().getInputAlphabet().stream().filter(s -> !(s instanceof TimeStepSymbol<String>)).toList());
    }

}
