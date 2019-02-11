/* Copyright (C) 2013-2019 TU Dortmund
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
package de.learnlib.examples.sli;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealy;
import de.learnlib.algorithms.lstar.mealy.PartialLStarMealyBuilder;
import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.oracle.StateLocalInputOracle.StateLocalInputMealyOracle;
import de.learnlib.driver.util.StateLocalInputMealySimulatorSUL;
import de.learnlib.examples.mealy.ExampleRandomStateLocalInputMealy;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.cache.mealy.MealyCaches;
import de.learnlib.filter.cache.mealy.StateLocalInputMealyCacheOracle;
import de.learnlib.filter.statistic.sul.ResetCounterStateLocalInputSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterStateLocalInputSUL;
import de.learnlib.oracle.equivalence.EQOracleChain;
import de.learnlib.oracle.equivalence.EQOracleChain.MealyEQOracleChain;
import de.learnlib.oracle.equivalence.SimulatorEQOracle.MealySimulatorEQOracle;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.Experiment.MealyExperiment;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.OutputAndLocalInputs;
import net.automatalib.automata.transducers.StateLocalInputMealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.transducers.StateLocalInputMealyUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * An example showcasing the performance impact of the {@link PartialLStarMealy} learning algorithm compared to the
 * regular {@link ExtensibleLStarMealy} when learning partial systems.
 *
 * @author frohme
 */
public final class Example2 {

    private static final Alphabet<Integer> INPUTS;
    private static final CompactMealy<Integer, Character> TARGET;
    private static final MealyMachine<?, Integer, ?, OutputAndLocalInputs<Integer, Character>> TRANSFORMED_TARGET;
    private static final StateLocalInputSUL<Integer, Character> SUL;

    static {
        final int alphabetSize = 10;
        INPUTS = Alphabets.integers(0, alphabetSize);

        final Character[] outputs = new Character[] {'a', 'b', 'c'};
        final ExampleRandomStateLocalInputMealy<Integer, Character> example =
                ExampleRandomStateLocalInputMealy.createExample(new Random(42), INPUTS, 100, outputs);

        TARGET = example.getReferenceAutomaton();
        TRANSFORMED_TARGET = StateLocalInputMealyUtil.partialToObservableOutput(TARGET);
        SUL = new StateLocalInputMealySimulatorSUL<>(TARGET);
    }

    private Example2() {
        // prevent instantiation
    }

    public static void main(String[] args) {
        runPartialLearner(false);
        runPartialLearner(true);
        runTransformedLearner(false);
        runTransformedLearner(true);
    }

    /**
     * Uses the raw {@link StateLocalInputSUL} to infer a (potentially) partial {@link MealyMachine}.
     */
    static void runPartialLearner(boolean withCache) {

        // setup SULs and counters
        StateLocalInputSUL<Integer, Character> target = SUL;

        ResetCounterStateLocalInputSUL<Integer, Character> resetCounter =
                new ResetCounterStateLocalInputSUL<>("Resets", target);
        SymbolCounterStateLocalInputSUL<Integer, Character> symbolCounter =
                new SymbolCounterStateLocalInputSUL<>("Symbols", resetCounter);

        // construct a (state local input) simulator membership query oracle
        StateLocalInputMealyOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqOracle =
                new StateLocalInputSULOracle<>(symbolCounter);

        // construct storage for EquivalenceOracle chain, because we want to use the potential cache as well
        List<EquivalenceOracle<StateLocalInputMealyMachine<?, Integer, ?, Character>, Integer, Word<OutputAndLocalInputs<Integer, Character>>>>
                eqOracles = new ArrayList<>(2);

        if (withCache) {
            StateLocalInputMealyCacheOracle<Integer, Character> mqCache =
                    MealyCaches.createStateLocalInputTreeCache(mqOracle.definedInputs(Word.epsilon()), mqOracle);
            eqOracles.add(mqCache.createStateLocalInputCacheConsistencyTest());
            mqOracle = mqCache;
        }

        // construct L* instance
        PartialLStarMealy<Integer, Character> lstar =
                new PartialLStarMealyBuilder<Integer, Character>().withOracle(mqOracle)
                                                                  .withCexHandler(ObservationTableCEXHandlers.RIVEST_SCHAPIRE)
                                                                  .create();

        // here, we simply fallback to an equivalence check for the original automaton model
        eqOracles.add(new StateLocalInputMealySimulatorEQOracle<>(TARGET));

        // construct single EQ oracle
        EquivalenceOracle<StateLocalInputMealyMachine<?, Integer, ?, Character>, Integer, Word<OutputAndLocalInputs<Integer, Character>>>
                eqOracle = new EQOracleChain<>(eqOracles);

        // construct the experiment
        // note, we can't use the regular MealyExperiment (or MooreExperiment) because the output type of our hypothesis
        // is different from our membership query type
        Experiment<StateLocalInputMealyMachine<?, Integer, ?, Character>> experiment =
                new Experiment<>(lstar, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // report results
        System.out.println("Partial Hypothesis" + (withCache ? ", with cache" : ""));
        System.out.println("-------------------------------------------------------");

        System.out.println(resetCounter.getStatisticalData().getSummary());
        System.out.println(symbolCounter.getStatisticalData().getSummary());

        System.out.println("-------------------------------------------------------");
    }

    /**
     * Uses the raw {@link StateLocalInputSUL} to infer a complete partial {@link MealyMachine} as in classical automata
     * learning. Note: here the {@link MealyMembershipOracle} needs to take care of mapping potentially undefined inputs
     * of the system under learning to processable outputs for the learner.
     */
    static void runTransformedLearner(boolean withCache) {

        // setup SULs and counters
        StateLocalInputSUL<Integer, Character> target = SUL;

        ResetCounterStateLocalInputSUL<Integer, Character> resetCounter =
                new ResetCounterStateLocalInputSUL<>("Resets", target);
        SymbolCounterStateLocalInputSUL<Integer, Character> symbolCounter =
                new SymbolCounterStateLocalInputSUL<>("Symbols", resetCounter);

        // construct a (regular) simulator membership query oracle
        MealyMembershipOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqOracle =
                new StateLocalInputSULOracle<>(symbolCounter);

        // construct storage for EquivalenceOracle chain, because we want to use the potential cache as well
        List<MealyEquivalenceOracle<Integer, OutputAndLocalInputs<Integer, Character>>> eqOracles = new ArrayList<>(2);

        if (withCache) {
            MealyCacheOracle<Integer, OutputAndLocalInputs<Integer, Character>> mqCache =
                    MealyCaches.createStateLocalInputTreeCache(INPUTS, mqOracle);
            eqOracles.add(mqCache.createCacheConsistencyTest());
            mqOracle = mqCache;
        }

        // construct L* instance
        ExtensibleLStarMealy<Integer, OutputAndLocalInputs<Integer, Character>> lstar =
                new ExtensibleLStarMealyBuilder<Integer, OutputAndLocalInputs<Integer, Character>>().withAlphabet(INPUTS)
                                                                                                    .withOracle(mqOracle)
                                                                                                    .withCexHandler(
                                                                                                            ObservationTableCEXHandlers.RIVEST_SCHAPIRE)
                                                                                                    .create();

        // here, we simply fallback to an equivalence check for the transformed automaton model
        eqOracles.add(new MealySimulatorEQOracle<>(TRANSFORMED_TARGET));

        // construct single EQ oracle
        MealyEquivalenceOracle<Integer, OutputAndLocalInputs<Integer, Character>> eqOracle =
                new MealyEQOracleChain<>(eqOracles);

        // construct the experiment
        MealyExperiment<Integer, OutputAndLocalInputs<Integer, Character>> experiment =
                new MealyExperiment<>(lstar, eqOracle, INPUTS);

        // run experiment
        experiment.run();

        // report results
        System.out.println("Transformed Hypothesis" + (withCache ? ", with cache" : ""));
        System.out.println("-------------------------------------------------------");

        System.out.println(resetCounter.getStatisticalData().getSummary());
        System.out.println(symbolCounter.getStatisticalData().getSummary());

        System.out.println("-------------------------------------------------------");
    }

}
