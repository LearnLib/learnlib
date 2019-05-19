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
package de.learnlib.filter.cache.parallelism;

import java.util.Random;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.dfa.DFACaches;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.util.automata.random.RandomAutomata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/**
 * @author frohme
 */
public class DFAParallelCacheTest extends AbstractParallelCacheTest<DFA<?, Character>, Character, Boolean> {

    @Override
    protected Alphabet<Character> getAlphabet() {
        return Alphabets.characters('a', 'e');
    }

    @Override
    protected DFA<?, Character> getTargetModel() {
        return RandomAutomata.randomDFA(new Random(42), MODEL_SIZE, getAlphabet());
    }

    @Override
    protected DFALearningCacheOracle<Character> getCache(Alphabet<Character> alphabet,
                                                         MembershipOracle<Character, Boolean> oracle) {
        return DFACaches.createCache(alphabet, oracle);
    }
}
