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
package de.learnlib.example.sli;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.filter.cache.sul.SULCache;
import de.learnlib.filter.cache.sul.SULCaches;
import de.learnlib.filter.cache.sul.StateLocalInputSULCache;
import de.learnlib.filter.statistic.sul.CounterSUL;
import de.learnlib.filter.statistic.sul.CounterStateLocalInputSUL;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import de.learnlib.testsupport.example.mealy.ExampleRandomStateLocalInputMealy;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
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
        final CounterStateLocalInputSUL<Integer, Character> counterSUL = new CounterStateLocalInputSUL<>(target);

        // construct storage for EquivalenceOracle chain, because we want to use the potential cache as well
        final List<MealyEquivalenceOracle<Integer, Character>> eqOracles = new ArrayList<>(2);

        final StateLocalInputSUL<Integer, Character> sul;

        if (withCache) {
            final StateLocalInputSULCache<Integer, Character> cache =
                    SULCaches.createStateLocalInputCache(INPUTS, counterSUL);
            eqOracles.add(cache.createCacheConsistencyTest());
            sul = cache;
        } else {
            sul = counterSUL;
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

        System.out.println(counterSUL.getStatisticalData().getSummary());

        System.out.println("-------------------------------------------------------");
    }

    /**
     * Uses the regular {@link SUL} interface that tries to perform unavailable transitions.
     */
    static void runNormalLearner(boolean withCache) {

        // setup SULs and counters
        final SUL<Integer, Character> target = new MealySimulatorSUL<>(TARGET, UNDEFINED);
        final CounterSUL<Integer, Character> counterSUL = new CounterSUL<>(target);

        // construct storage for EquivalenceOracle chain, because we want to use the potential cache as well
        final List<MealyEquivalenceOracle<Integer, Character>> eqOracles = new ArrayList<>(2);

        final SUL<Integer, Character> sul;

        if (withCache) {
            final SULCache<Integer, Character> cache = SULCaches.createCache(INPUTS, counterSUL);
            eqOracles.add(cache.createCacheConsistencyTest());
            sul = cache;
        } else {
            sul = counterSUL;
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

        System.out.println(counterSUL.getStatisticalData().getSummary());

        System.out.println("-------------------------------------------------------");
    }

}
