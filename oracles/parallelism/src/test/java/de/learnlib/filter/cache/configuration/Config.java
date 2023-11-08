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
package de.learnlib.filter.cache.configuration;

import java.util.Random;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.filter.statistic.oracle.MooreCounterOracle;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.ResetCounterStateLocalInputSUL;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.oracle.membership.MooreSimulatorOracle;
import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.CompactMealy;
import net.automatalib.automaton.transducer.CompactMoore;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.util.automaton.random.RandomAutomata;

public final class Config {

    public static final Alphabet<Character> ALPHABET;
    public static final DFA<?, Character> TARGET_MODEL_DFA;
    public static final MealyMachine<?, Character, ?, Character> TARGET_MODEL_MEALY;
    public static final MooreMachine<?, Character, ?, Character> TARGET_MODEL_MOORE;
    public static final SUL<Character, Character> TARGET_MODEL_SUL;
    public static final StateLocalInputSUL<Character, Character> TARGET_MODEL_SLI_SUL;

    static {
        ALPHABET = Alphabets.characters('a', 'e');
        TARGET_MODEL_DFA = RandomAutomata.randomDFA(new Random(42), 10, ALPHABET);

        final CompactMealy<Character, Character> mealy =
                RandomAutomata.randomMealy(new Random(42), 10, ALPHABET, ALPHABET);

        final CompactMoore<Character, Character> moore =
                RandomAutomata.randomMoore(new Random(42), 10, ALPHABET, ALPHABET);

        TARGET_MODEL_MEALY = mealy;
        TARGET_MODEL_MOORE = moore;
        TARGET_MODEL_SUL = new MealySimulatorSUL<>(mealy);
        TARGET_MODEL_SLI_SUL = new StateLocalInputMealySimulatorSUL<>(mealy);
    }

    private Config() {}

    public static <I> DFACounterOracle<I> getCounter(DFA<?, I> delegate) {
        return new DFACounterOracle<>(new DFASimulatorOracle<>(delegate));
    }

    public static <I, O> MealyCounterOracle<I, O> getCounter(MealyMachine<?, I, ?, O> delegate) {
        return new MealyCounterOracle<>(new MealySimulatorOracle<>(delegate));
    }

    public static <I, O> MooreCounterOracle<I, O> getCounter(MooreMachine<?, I, ?, O> delegate) {
        return new MooreCounterOracle<>(new MooreSimulatorOracle<>(delegate));
    }

    public static <I, O> ResetCounterSUL<I, O> getCounter(SUL<I, O> delegate) {
        return new ResetCounterSUL<>("Queries", delegate);
    }

    public static <I, O> ResetCounterStateLocalInputSUL<I, O> getCounter(StateLocalInputSUL<I, O> delegate) {
        return new ResetCounterStateLocalInputSUL<>("Queries", delegate);
    }
}
