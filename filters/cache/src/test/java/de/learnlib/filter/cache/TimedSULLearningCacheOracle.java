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

import java.util.Collection;

import de.learnlib.filter.cache.LearningCache.MMLTLearningCache;
import de.learnlib.filter.cache.LearningCacheOracle.MMMLTLearningCacheOracle;
import de.learnlib.filter.cache.mmlt.TimedSULTreeCache;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.query.Query;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

public class TimedSULLearningCacheOracle<I, O, C extends MMLTLearningCache<I, O>>
        implements MMMLTLearningCacheOracle<I, O> {

    private final C cache;
    private final TimedQueryOracle<I, O> oracle;

    public TimedSULLearningCacheOracle(C cache, TimedQueryOracle<I, O> oracle) {
        this.cache = cache;
        this.oracle = oracle;
    }

    @Override
    public void processQueries(Collection<? extends Query<TimedInput<I>, Word<TimedOutput<O>>>> queries) {
        oracle.processQueries(queries);
    }

    @Override
    public EquivalenceOracle<MMLT<?, I, ?, O>, TimedInput<I>, Word<TimedOutput<O>>> createCacheConsistencyTest() {
        return cache.createCacheConsistencyTest();
    }

    public C getCache() {
        return cache;
    }

    public TimedQueryOracle<I, O> getOracle() {
        return oracle;
    }

    @Override
    public TimerQueryResult<O> queryTimers(Word<TimedInput<I>> prefix, long maxTotalWaitingTime) {
        return oracle.queryTimers(prefix, maxTotalWaitingTime);
    }

    public static <I, O> TimedSULLearningCacheOracle<I, O, TimedSULTreeCache<I, O>> fromTimedSULCache(TimedSULTreeCache<I, O> cache,
                                                                                                      MMLTModelParams<O> params) {
        return new TimedSULLearningCacheOracle<>(cache, new TimedSULOracle<>(cache, params));
    }

}
