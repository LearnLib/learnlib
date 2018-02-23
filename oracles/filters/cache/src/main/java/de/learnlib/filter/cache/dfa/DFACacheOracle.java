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
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.words.Alphabet;

/**
 * DFA cache. This cache is implemented as a membership oracle: upon construction, it is provided with a delegate
 * oracle. Queries that can be answered from the cache are answered directly, others are forwarded to the delegate
 * oracle. When the delegate oracle has finished processing these remaining queries, the results are incorporated into
 * the cache.
 *
 * @param <I>
 *         input symbol class
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public class DFACacheOracle<I> implements DFALearningCacheOracle<I> {

    private final IncrementalDFABuilder<I> incDfa;
    private final Lock incDfaLock;
    private final MembershipOracle<I, Boolean> delegate;

    DFACacheOracle(IncrementalDFABuilder<I> incDfa, MembershipOracle<I, Boolean> delegate) {
        this(incDfa, new ReentrantLock(), delegate);
    }

    DFACacheOracle(IncrementalDFABuilder<I> incDfa, Lock lock, MembershipOracle<I, Boolean> delegate) {
        this.incDfa = incDfa;
        this.incDfaLock = lock;
        this.delegate = delegate;
    }

    public static <I> DFACacheOracle<I> createTreeCacheOracle(Alphabet<I> alphabet,
                                                              MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalDFATreeBuilder<>(alphabet), delegate);
    }

    public static <I> DFACacheOracle<I> createTreePCCacheOracle(Alphabet<I> alphabet,
                                                                MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalPCDFATreeBuilder<>(alphabet), delegate);
    }

    public static <I> DFACacheOracle<I> createDAGCacheOracle(Alphabet<I> alphabet,
                                                             MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalDFADAGBuilder<>(alphabet), delegate);
    }

    public static <I> DFACacheOracle<I> createDAGPCCacheOracle(Alphabet<I> alphabet,
                                                               MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalPCDFADAGBuilder<>(alphabet), delegate);
    }

    /**
     * Creates an equivalence oracle that checks an hypothesis for consistency with the contents of this cache. Note
     * that the returned oracle is backed by the cache data structure, i.e., it is sufficient to call this method once
     * after creation of the cache.
     *
     * @return the cache consistency test backed by the contents of this cache.
     */
    @Override
    public DFACacheConsistencyTest<I> createCacheConsistencyTest() {
        return new DFACacheConsistencyTest<>(incDfa, incDfaLock);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
        List<ProxyQuery<I>> unanswered = new ArrayList<>();

        incDfaLock.lock();
        try {
            for (Query<I, Boolean> q : queries) {
                Acceptance acc = incDfa.lookup(q.getInput());
                if (acc != Acceptance.DONT_KNOW) {
                    q.answer(acc.toBoolean());
                } else {
                    unanswered.add(new ProxyQuery<>(q));
                }
            }
        } finally {
            incDfaLock.unlock();
        }

        delegate.processQueries(unanswered);

        incDfaLock.lock();
        try {
            for (ProxyQuery<I> q : unanswered) {
                incDfa.insert(q.getInput(), q.getAnswer());
            }
        } finally {
            incDfaLock.unlock();
        }
    }

}
