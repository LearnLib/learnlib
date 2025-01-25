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
package de.learnlib.filter.cache;

import java.util.Random;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.filter.statistic.oracle.MooreCounterOracle;
import de.learnlib.filter.statistic.sul.CounterSUL;
import de.learnlib.filter.statistic.sul.CounterStateLocalInputSUL;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.oracle.membership.MooreSimulatorOracle;
import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.impl.CompactDFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.util.automaton.random.RandomAutomata;

public final class CacheTestUtils {

    public static final Alphabet<Character> INPUT_ALPHABET;
    public static final Alphabet<Character> EXTENSION_ALPHABET;
    public static final Alphabet<Integer> OUTPUT_ALPHABET;

    public static final CompactDFA<Character> DFA;
    public static final CompactDFA<Character> DFA_INVALID;
    public static final CompactMealy<Character, Integer> MEALY;
    public static final CompactMealy<Character, Integer> MEALY_INVALID;
    public static final CompactMoore<Character, Integer> MOORE;
    public static final CompactMoore<Character, Integer> MOORE_INVALID;

    public static final SUL<Character, Integer> SUL;
    public static final StateLocalInputSUL<Character, Integer> SLI_SUL;

    static {
        INPUT_ALPHABET = Alphabets.characters('a', 'c');
        EXTENSION_ALPHABET = Alphabets.characters('d', 'e');
        OUTPUT_ALPHABET = Alphabets.integers(1, 4);

        final Alphabet<Character> combinedAlphabet = Alphabets.characters('a', 'e');
        final Random random = new Random(42);
        final int size = 20;
        DFA = RandomAutomata.randomDFA(random, size, combinedAlphabet);
        MEALY = RandomAutomata.randomMealy(random, size, combinedAlphabet, OUTPUT_ALPHABET);
        MOORE = RandomAutomata.randomMoore(random, size, combinedAlphabet, OUTPUT_ALPHABET);

        DFA_INVALID = new CompactDFA<>(DFA);
        DFA_INVALID.flipAcceptance();

        // we rely on two generations not producing the same automaton
        MEALY_INVALID = RandomAutomata.randomMealy(random, size, combinedAlphabet, OUTPUT_ALPHABET);
        MOORE_INVALID = RandomAutomata.randomMoore(random, size, combinedAlphabet, OUTPUT_ALPHABET);

        SUL = new MealySimulatorSUL<>(MEALY);
        SLI_SUL = new StateLocalInputMealySimulatorSUL<>(MEALY);
    }

    private CacheTestUtils() {}

    public static <I> DFACounterOracle<I> getCounter(net.automatalib.automaton.fsa.DFA<?, I> delegate) {
        return new DFACounterOracle<>(new DFASimulatorOracle<>(delegate));
    }

    public static <I, O> MealyCounterOracle<I, O> getCounter(MealyMachine<?, I, ?, O> delegate) {
        return new MealyCounterOracle<>(new MealySimulatorOracle<>(delegate));
    }

    public static <I, O> MooreCounterOracle<I, O> getCounter(MooreMachine<?, I, ?, O> delegate) {
        return new MooreCounterOracle<>(new MooreSimulatorOracle<>(delegate));
    }

    public static <I, O> CounterSUL<I, O> getCounter(SUL<I, O> delegate) {
        return new CounterSUL<>(delegate);
    }

    public static <I, O> CounterStateLocalInputSUL<I, O> getCounter(StateLocalInputSUL<I, O> delegate) {
        return new CounterStateLocalInputSUL<>(delegate);
    }
}
