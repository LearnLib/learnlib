/* Copyright (C) 2013-2021 TU Dortmund
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.learnlib.api.Resumable;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.dfa.DFACacheOracle.DFACacheOracleState;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.words.Alphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DFACacheOracle<I>
        implements DFALearningCacheOracle<I>, SupportsGrowingAlphabet<I>, Resumable<DFACacheOracleState<I>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DFACacheOracle.class);

    private IncrementalDFABuilder<I> incDfa;
    private final ReadWriteLock incDfaLock;
    private final MembershipOracle<I, Boolean> delegate;

    DFACacheOracle(IncrementalDFABuilder<I> incDfa, MembershipOracle<I, Boolean> delegate) {
        this.incDfa = incDfa;
        this.incDfaLock = new ReentrantReadWriteLock();
        this.delegate = delegate;
    }

    /**
     * Creates a cache oracle for a DFA learning setup, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param delegate
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalDFATreeBuilder
     */
    public static <I> DFACacheOracle<I> createTreeCacheOracle(Alphabet<I> alphabet,
                                                              MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalDFATreeBuilder<>(alphabet), delegate);
    }

    /**
     * Creates a prefix-closed cache oracle for a DFA learning setup, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param delegate
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalPCDFATreeBuilder
     */
    public static <I> DFACacheOracle<I> createTreePCCacheOracle(Alphabet<I> alphabet,
                                                                MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalPCDFATreeBuilder<>(alphabet), delegate);
    }

    /**
     * Creates a cache oracle for a DFA learning setup, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param delegate
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalDFADAGBuilder
     */
    public static <I> DFACacheOracle<I> createDAGCacheOracle(Alphabet<I> alphabet,
                                                             MembershipOracle<I, Boolean> delegate) {
        return new DFACacheOracle<>(new IncrementalDFADAGBuilder<>(alphabet), delegate);
    }

    /**
     * Creates a prefix-closed cache oracle for a DFA learning setup, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param delegate
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalPCDFADAGBuilder
     */
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

        incDfaLock.readLock().lock();
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
            incDfaLock.readLock().unlock();
        }

        delegate.processQueries(unanswered);

        incDfaLock.writeLock().lock();
        try {
            for (ProxyQuery<I> q : unanswered) {
                incDfa.insert(q.getInput(), q.getAnswer());
            }
        } finally {
            incDfaLock.writeLock().unlock();
        }
    }

    @Override
    public void addAlphabetSymbol(I symbol) {
        incDfa.addAlphabetSymbol(symbol);
    }

    @Override
    public DFACacheOracleState<I> suspend() {
        return new DFACacheOracleState<>(incDfa);
    }

    @Override
    public void resume(DFACacheOracleState<I> state) {
        final Class<?> thisClass = this.incDfa.getClass();
        final Class<?> stateClass = state.getBuilder().getClass();

        if (!thisClass.equals(stateClass)) {
            LOGGER.warn(
                    "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                    thisClass,
                    stateClass);
        }

        this.incDfa = state.getBuilder();
    }

    public static class DFACacheOracleState<I> {

        private final IncrementalDFABuilder<I> builder;

        DFACacheOracleState(IncrementalDFABuilder<I> builder) {
            this.builder = builder;
        }

        IncrementalDFABuilder<I> getBuilder() {
            return builder;
        }
    }
}
