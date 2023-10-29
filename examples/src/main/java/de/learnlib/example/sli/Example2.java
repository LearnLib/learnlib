/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.example.sli;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.example.mealy.ExampleRandomStateLocalInputMealy;
import de.learnlib.filter.cache.sul.SULCache;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.cache.sul.StateLocalInputSULCache;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.ResetCounterStateLocalInputSUL;
import de.learnlib.filter.statistic.sul.SLICounterStateLocalInputSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterStateLocalInputSUL;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.transducer.StateLocalInputMealyMachine;

/**
 * An example showcasing the performance impact of the {@link StateLocalInputSUL} interface compared to the regular
 * {@link SUL} interface which continues querying over potentially undefined transitions.
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class Example2 {

    private static final Alphabet<Integer> INPUTS;
    private static final StateLocalInputMealyMachine<?, Integer, ?, Character> TARGET;
    private static final Character UNDEFINED;

    static {
        final int alphabetSize = 10;
        INPUTS = Alphabets.integers(0, alphabetSize);

        final Character[] outputs = {'a', 'b', 'c'};
        UNDEFINED = 'u';

        final ExampleRandomStateLocalInputMealy<Integer, Character> example =
                ExampleRandomStateLocalInputMealy.createExample(new Random(42), INPUTS, 100, UNDEFINED, outputs);

        TARGET = example.getReferenceAutomaton();
    }

    private Example2() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        runSLILearner(false);
        runSLILearner(true);
        runNormalLearner(false);
        runNormalLearner(true);
    }

    /**
     * Uses the {@link StateLocalInputSUL} interface to filter undefined transitions.
     */
    static void runSLILearner(boolean withCache) {

        // setup SULs and counters
        final StateLocalInputSUL<Integer, Character> target = new StateLocalInputMealySimulatorSUL<>(TARGET);

        final ResetCounterStateLocalInputSUL<Integer, Character> resetCounter =
                new ResetCounterStateLocalInputSUL<>("Resets", target);
        final SymbolCounterStateLocalInputSUL<Integer, Character> symbolCounter =
                new SymbolCounterStateLocalInputSUL<>("Symbols", resetCounter);
        final SLICounterStateLocalInputSUL<Integer, Character> sliCounter =
                new SLICounterStateLocalInputSUL<>("State Local Inputs", symbolCounter);

        // construct storage for EquivalenceOracle chain, because we want to use the potential cache as well
        final List<MealyEquivalenceOracle<Integer, Character>> eqOracles = new ArrayList<>(2);

        final StateLocalInputSUL<Integer, Character> sul;

        if (withCache) {
            final StateLocalInputSULCache<Integer, Character> cache =
                    SULCaches.createStateLocalInputCache(INPUTS, sliCounter);
            eqOracles.add(cache.createCacheConsistencyTest());
            sul = cache;
        } else {
            sul = sliCounter;
        }

        // construct a (state local input) SUL oracle which answers undefined transitions with the undefined symbol
        final MealyMembershipOracle<Integer, Character> mqOracle = new StateLocalInputSULOracle<>(sul, UNDEFINED);

        // construct learner instance
        final MealyLearner<Integer, Character> learner =
                new ExtensibleLStarMealyBuilder<Integer, Character>().withAlphabet(INPUTS)
                                                                     .withOracle(mqOracle)
                                                                     .create();

        // since we want to compare structural differences between our partial target and the total hypothesis, we use a
        // specific SLI equivalence oracle. Any EQ oracle based on output behavior (e.g. W(p) method) would work as
        // well.
        eqOracles.add(new StateLocalInputMealySimulatorEQOracle<>(TARGET, INPUTS, UNDEFINED));

        // build EQ oracle chain
        final MealyEquivalenceOracle<Integer, Character> eqOracle = new MealyEQOracleChain<>(eqOracles);

        // construct the experiment
        MealyExperiment<Integer, Character> experiment = new MealyExperiment<>(learner, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // report results
        System.out.println("State Local Input SUL" + (withCache ? ", with cache" : ""));
        System.out.println("-------------------------------------------------------");

        System.out.println(resetCounter.getStatisticalData().getSummary());
        System.out.println(symbolCounter.getStatisticalData().getSummary());
        System.out.println(sliCounter.getStatisticalData().getSummary());

        System.out.println("-------------------------------------------------------");
    }

    /**
     * Uses the regular {@link SUL} interface that tries to perform unavailable transitions.
     */
    static void runNormalLearner(boolean withCache) {

        // setup SULs and counters
        final SUL<Integer, Character> target = new MealySimulatorSUL<>(TARGET, UNDEFINED);

        final ResetCounterSUL<Integer, Character> resetCounter = new ResetCounterSUL<>("Resets", target);
        final SymbolCounterSUL<Integer, Character> symbolCounter = new SymbolCounterSUL<>("Symbols", resetCounter);

        // construct storage for EquivalenceOracle chain, because we want to use the potential cache as well
        final List<MealyEquivalenceOracle<Integer, Character>> eqOracles = new ArrayList<>(2);

        final SUL<Integer, Character> sul;

        if (withCache) {
            final SULCache<Integer, Character> cache = SULCaches.createCache(INPUTS, symbolCounter);
            eqOracles.add(cache.createCacheConsistencyTest());
            sul = cache;
        } else {
            sul = symbolCounter;
        }

        // construct a (regular) simulator membership query oracle
        final MealyMembershipOracle<Integer, Character> mqOracle = new SULOracle<>(sul);

        // construct learner instance
        final MealyLearner<Integer, Character> learner =
                new ExtensibleLStarMealyBuilder<Integer, Character>().withAlphabet(INPUTS)
                                                                     .withOracle(mqOracle)
                                                                     .create();

        // since we want to compare structural differences between our partial target and the total hypothesis, we use a
        // specific SLI equivalence oracle. Any EQ oracle based on output behavior (e.g. W(p) method) would work as
        // well.
        eqOracles.add(new StateLocalInputMealySimulatorEQOracle<>(TARGET, INPUTS, UNDEFINED));

        // build EQ oracle chain
        final MealyEquivalenceOracle<Integer, Character> eqOracle = new MealyEQOracleChain<>(eqOracles);

        // construct the experiment
        final MealyExperiment<Integer, Character> experiment = new MealyExperiment<>(learner, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // report results
        System.out.println("Regular SUL" + (withCache ? ", with cache" : ""));
        System.out.println("-------------------------------------------------------");

        System.out.println(resetCounter.getStatisticalData().getSummary());
        System.out.println(symbolCounter.getStatisticalData().getSummary());

        System.out.println("-------------------------------------------------------");
    }

}
