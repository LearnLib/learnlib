/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.testsupport;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.QueryAnswerer;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.cache.mealy.MealyCaches;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public abstract class AbstractGrowingAlphabetMealyTest<L extends SupportsGrowingAlphabet<Character> & LearningAlgorithm<MealyMachine<?, Character, ?, Character>, Character, Word<Character>>>
        extends AbstractGrowingAlphabetTest<L, MealyMachine<?, Character, ?, Character>, MembershipOracle<Character, Word<Character>>, Character, Word<Character>> {

    @Override
    protected Alphabet<Character> getInitialAlphabet() {
        return Alphabets.characters('0', '4');
    }

    @Override
    protected Collection<Character> getAlphabetExtensions() {
        return Alphabets.characters('5', '9');
    }

    @Override
    protected MealyMachine<?, Character, ?, Character> getTarget(Alphabet<Character> alphabet) {
        return RandomAutomata.randomMealy(new Random(RANDOM_SEED),
                                          DEFAULT_AUTOMATON_SIZE,
                                          alphabet,
                                          Alphabets.characters('a', 'f'));
    }

    @Override
    protected MembershipOracle<Character, Word<Character>> getOracle(MealyMachine<?, Character, ?, Character> target) {
        return ((QueryAnswerer<Character, Word<Character>>) target::computeSuffixOutput).asOracle();
    }

    @Override
    protected MembershipOracle<Character, Word<Character>> getCachedOracle(Alphabet<Character> alphabet,
                                                                           MembershipOracle<Character, Word<Character>> source,
                                                                           List<Consumer<Character>> symbolListener) {
        final MealyCacheOracle<Character, Character> cache = MealyCaches.createDAGCache(alphabet, source);
        symbolListener.add(cache::addAlphabetSymbol);
        return cache;
    }
}
