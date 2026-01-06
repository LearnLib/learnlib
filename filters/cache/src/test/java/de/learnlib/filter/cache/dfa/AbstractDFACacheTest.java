/* Copyright (C) 2013-2026 TU Dortmund University
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
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.statistic.Statistics;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;

public abstract class AbstractDFACacheTest
        extends AbstractCacheTest<DFACacheOracle<Character>, DFA<?, Character>, Character, Boolean> {

    private final DFACounterOracle<Character> counter;

    public AbstractDFACacheTest() {
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
    protected DFACacheOracle<Character> getCachedOracle() {
        return getCache(counter);
    }

    @Override
    protected DFACacheOracle<Character> getResumedOracle(DFACacheOracle<Character> original) {
        final DFACacheOracle<Character> fresh = getCache(counter);
        serializeResumable(original, fresh);
        return fresh;
    }

    @Override
    protected Boolean computeOutput(DFA<?, Character> model, Word<Character> input) {
        return model.computeOutput(input);
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return Statistics.getService().getCount(DFACounterOracle.KEY_QUERY).orElse(0L);
    }

    @Override
    protected boolean supportsPrefixes() {
        return false;
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

    protected abstract DFACacheOracle<Character> getCache(DFAMembershipOracle<Character> delegate);

}
