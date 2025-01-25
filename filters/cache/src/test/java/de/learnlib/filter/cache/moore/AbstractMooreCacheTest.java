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
package de.learnlib.filter.cache.moore;

import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.MooreCounterOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.oracle.membership.MooreSimulatorOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractMooreCacheTest
        extends AbstractCacheTest<MooreCacheOracle<Character, Integer>, MooreMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final MooreCounterOracle<Character, Integer> counter;
    protected final Mapping<Integer, @Nullable Integer> errorMapper;

    public AbstractMooreCacheTest() {
        counter = new MooreCounterOracle<>(new MooreSimulatorOracle<>(CacheTestUtils.MOORE));
        errorMapper = o -> o == 1 ? 10 : null;
    }

    @Override
    protected MooreMachine<?, Character, ?, Integer> getTargetModel() {
        return CacheTestUtils.MOORE;
    }

    @Override
    protected MooreMachine<?, Character, ?, Integer> getInvalidTargetModel() {
        return CacheTestUtils.MOORE_INVALID;
    }

    @Override
    protected MooreCacheOracle<Character, Integer> getCachedOracle() {
        return getCache(counter);
    }

    @Override
    protected MooreCacheOracle<Character, Integer> getResumedOracle(MooreCacheOracle<Character, Integer> original) {
        final MooreCacheOracle<Character, Integer> fresh = getCache(counter);
        serializeResumable(original, fresh);
        return fresh;
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getQueryCounter().getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return true;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return new GrowingMapAlphabet<>(CacheTestUtils.INPUT_ALPHABET);
    }

    @Override
    protected Alphabet<Character> getExtensionAlphabet() {
        return CacheTestUtils.EXTENSION_ALPHABET;
    }

    @Override
    protected boolean supportsGrowing() {
        return true;
    }

    protected abstract MooreCacheOracle<Character, Integer> getCache(MooreMembershipOracle<Character, Integer> delegate);
}
