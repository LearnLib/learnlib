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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.learnlib.Resumable;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.dfa.DFAHashCacheOracle.DFAHashCacheOracleState;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.word.Word;

/**
 * A {@link DFALearningCacheOracle} that uses a {@link Map} for internal cache organization.
 * <p>
 * <b>Note:</b> this implementation is <b>not</b> thread-safe. If you require a cache that is usable in a parallel
 * environment. consider using the alternatives offered by the {@link ThreadSafeDFACaches} factory.
 *
 * @param <I>
 *         input symbol type
 */
public class DFAHashCacheOracle<I> implements DFALearningCacheOracle<I>, Resumable<DFAHashCacheOracleState<I>> {

    private final MembershipOracle<I, Boolean> delegate;
    private Map<Word<I>, Boolean> cache;

    DFAHashCacheOracle(MembershipOracle<I, Boolean> delegate) {
        this(delegate, new HashMap<>());
    }

    DFAHashCacheOracle(MembershipOracle<I, Boolean> delegate, Map<Word<I>, Boolean> cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    @Override
    public EquivalenceOracle<DFA<?, I>, I, Boolean> createCacheConsistencyTest() {
        return new DFAHashCacheConsistencyTest<>(cache);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
        final List<ProxyQuery<I>> misses = new ArrayList<>();
        final List<Query<I, Boolean>> duplicates = new ArrayList<>();
        final Set<Word<I>> batchCache = new HashSet<>();

        for (Query<I, Boolean> qry : queries) {
            final Word<I> input = qry.getInput();
            final Boolean answer = cache.get(input);
            if (answer != null) {
                qry.answer(answer);
            } else {
                if (batchCache.add(input)) { // never seen before
                    misses.add(new ProxyQuery<>(qry));
                } else {
                    duplicates.add(qry);
                }
            }
        }

        delegate.processQueries(misses);

        for (ProxyQuery<I> miss : misses) {
            cache.put(miss.getInput(), miss.getAnswer());
        }

        if (!duplicates.isEmpty()) {
            for (Query<I, Boolean> d : duplicates) {
                d.answer(cache.get(d.getInput()));
            }
        }
    }

    @Override
    public DFAHashCacheOracleState<I> suspend() {
        return new DFAHashCacheOracleState<>(cache);
    }

    @Override
    public void resume(DFAHashCacheOracleState<I> state) {
        this.cache = state.getCache();
    }

    public static class DFAHashCacheOracleState<I> {

        private final Map<Word<I>, Boolean> cache;

        public DFAHashCacheOracleState(Map<Word<I>, Boolean> cache) {
            this.cache = cache;
        }

        public Map<Word<I>, Boolean> getCache() {
            return cache;
        }
    }

}
