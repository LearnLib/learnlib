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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;

public class DFAHashCacheOracle<I> implements DFALearningCacheOracle<I> {

    private final MembershipOracle<I, Boolean> delegate;
    private final Map<Word<I>, Boolean> cache;
    private final Lock cacheLock;

    public DFAHashCacheOracle(MembershipOracle<I, Boolean> delegate) {
        this.delegate = delegate;
        this.cache = new HashMap<>();
        this.cacheLock = new ReentrantLock();
    }

    @Override
    public EquivalenceOracle<DFA<?, I>, I, Boolean> createCacheConsistencyTest() {
        return new DFAHashCacheConsistencyTest<>(cache, cacheLock);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
        List<ProxyQuery<I>> misses = new ArrayList<>();

        cacheLock.lock();
        try {
            for (Query<I, Boolean> qry : queries) {
                Word<I> input = qry.getInput();
                Boolean answer = cache.get(input);
                if (answer != null) {
                    qry.answer(answer);
                } else {
                    misses.add(new ProxyQuery<>(qry));
                }
            }
        } finally {
            cacheLock.unlock();
        }

        delegate.processQueries(misses);

        cacheLock.lock();
        try {
            for (ProxyQuery<I> miss : misses) {
                cache.put(miss.getInput(), miss.getAnswer());
            }
        } finally {
            cacheLock.unlock();
        }
    }

}
