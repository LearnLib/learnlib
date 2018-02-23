/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.filter.cache.dfa;

import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.filter.cache.AbstractCacheTest;
import de.learnlib.filter.cache.CacheTestUtils;
import de.learnlib.filter.cache.LearningCacheOracle;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.statistic.oracle.CounterOracle.DFACounterOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
public abstract class AbstractDFACacheTest extends AbstractCacheTest<DFA<?, Character>, Character, Boolean> {

    private final DFACounterOracle<Character> counter;

    public AbstractDFACacheTest() {
        counter = new DFACounterOracle<>(new DFASimulatorOracle<>(CacheTestUtils.DFA), "counterOracle");
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
    protected LearningCacheOracle<DFA<?, Character>, Character, Boolean> getCachedOracle() {
        return getCache(counter);
    }

    @Override
    protected long getNumberOfPosedQueries() {
        return counter.getCount();
    }

    @Override
    protected boolean supportsPrefixes() {
        return false;
    }

    @Override
    protected Alphabet<Character> getAlphabet() {
        return CacheTestUtils.INPUT_ALPHABET;
    }

    protected abstract DFALearningCacheOracle<Character> getCache(DFAMembershipOracle<Character> delegate);

}
