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
package de.learnlib.filter.cache.dfa;

import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;

public class DFAHashCacheTest
        extends AbstractCacheTest<DFAHashCacheOracle<Character>, DFA<?, Character>, Character, Boolean> {

    private final DFACounterOracle<Character> counter;

    public DFAHashCacheTest() {
        counter = new DFACounterOracle<>(new DFASimulatorOracle<>(CacheTestUtils.DFA));
    }

    @Override
    protected DFA<?, Character> getTargetModel() {
        return CacheTestUtils.DFA;
    }

    @Override
    protected DFA<?, Character> getInvalidTargetModel() {
        return CacheTestUtils.DFA_INVALID;
    }

    @Override
    protected DFAHashCacheOracle<Character> getCachedOracle() {
        return DFACaches.createHashCache(counter);
    }

    @Override
    protected DFAHashCacheOracle<Character> getResumedOracle(DFAHashCacheOracle<Character> original) {
        final DFAHashCacheOracle<Character> fresh = DFACaches.createHashCache(counter);
        serializeResumable(original, fresh);
        return fresh;
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getQueryCounter().getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return false;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    @Override
    protected Alphabet<Character> getExtensionAlphabet() {
        return CacheTestUtils.EXTENSION_ALPHABET;
    }

    @Override
    protected boolean supportsGrowing() {
        return false;
    }
}
