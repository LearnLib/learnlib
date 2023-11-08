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
package de.learnlib.algorithm.adt.learner;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import de.learnlib.algorithm.adt.config.ADTExtenders;
import de.learnlib.algorithm.adt.config.LeafSplitters;
import de.learnlib.algorithm.adt.config.SubtreeReplacers;
import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.oracle.SymbolQueryOracle;
import de.learnlib.oracle.membership.SULSymbolQueryOracle;
import de.learnlib.testsupport.AbstractGrowingAlphabetTest;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.Alphabets;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.util.automaton.random.RandomAutomata;
import net.automatalib.word.Word;

public class ADTGrowingAlphabetTest
        extends AbstractGrowingAlphabetTest<ADTLearner<Integer, Character>, MealyMachine<?, Integer, ?, Character>, SymbolQueryOracle<Integer, Character>, Integer, Word<Character>> {

    @Override
    protected Alphabet<Integer> getInitialAlphabet() {
        return Alphabets.integers(1, 5);
    }

    @Override
    protected Collection<Integer> getAlphabetExtensions() {
        return Alphabets.integers(6, 10);
    }

    @Override
    protected MealyMachine<?, Integer, ?, Character> getTarget(Alphabet<Integer> alphabet) {
        return RandomAutomata.randomMealy(new Random(42), 15, alphabet, Alphabets.characters('a', 'f'));
    }

    @Override
    protected SymbolQueryOracle<Integer, Character> getOracle(MealyMachine<?, Integer, ?, Character> target) {
        return new SULSymbolQueryOracle<>(new MealySimulatorSUL<>(target));
    }

    @Override
    protected ADTLearner<Integer, Character> getLearner(SymbolQueryOracle<Integer, Character> oracle,
                                                        Alphabet<Integer> alphabet) {
        return new ADTLearner<>(alphabet,
                                oracle,
                                LeafSplitters.DEFAULT_SPLITTER,
                                ADTExtenders.NOP,
                                SubtreeReplacers.NEVER_REPLACE);
    }

    @Override
    protected SymbolQueryOracle<Integer, Character> getCachedOracle(Alphabet<Integer> alphabet,
                                                                    SymbolQueryOracle<Integer, Character> original,
                                                                    List<Consumer<Integer>> symbolListener) {
        // ADT learner already uses a cache internally.
        return original;
    }

}
