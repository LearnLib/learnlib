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
package de.learnlib.filter.cache.mealy;

import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.statistic.oracle.MealyCounterOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class AbstractMealyCacheTest
        extends AbstractCacheTest<MealyCacheOracle<Character, Integer>, MealyMachine<?, Character, ?, Integer>, Character, Word<Integer>> {

    private final MealyCounterOracle<Character, Integer> counter;
    protected final Mapping<Integer, @Nullable Integer> errorMapper;

    public AbstractMealyCacheTest() {
        counter = new MealyCounterOracle<>(new MealySimulatorOracle<>(CacheTestUtils.MEALY));
        errorMapper = o -> o == 1 ? 10 : null;
    }

    @Override
    protected MealyMachine<?, Character, ?, Integer> getTargetModel() {
        return CacheTestUtils.MEALY;
    }

    @Override
    protected MealyMachine<?, Character, ?, Integer> getInvalidTargetModel() {
        return CacheTestUtils.MEALY_INVALID;
    }

    @Override
    protected MealyCacheOracle<Character, Integer> getCachedOracle() {
        return getCache(counter);
    }

    @Override
    protected MealyCacheOracle<Character, Integer> getResumedOracle(MealyCacheOracle<Character, Integer> original) {
        final MealyCacheOracle<Character, Integer> fresh = getCache(counter);
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

    protected abstract MealyCacheOracle<Character, Integer> getCache(MealyMembershipOracle<Character, Integer> delegate);
}
