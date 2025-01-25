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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.learnlib.Resumable;
import de.learnlib.filter.cache.LearningCacheOracle.DFALearningCacheOracle;
import de.learnlib.filter.cache.dfa.DFACacheOracle.DFACacheOracleState;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle.DFAEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.Query;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.common.util.Pair;
import net.automatalib.incremental.dfa.Acceptance;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DFA cache. This cache is implemented as a membership oracle: upon construction, it is provided with a delegate
 * oracle. Queries that can be answered from the cache are answered directly, others are forwarded to the delegate
 * oracle. When the delegate oracle has finished processing these remaining queries, the results are incorporated into
 * the cache.
 * <p>
 * <b>Note:</b> this implementation is <b>not</b> thread-safe. If you require a cache that is usable in a parallel
 * environment. use the {@code ThreadSafeDFACacheOracle} (or rather the {@code ThreadSafeDFACaches} factory) from the
 * {@code learnlib-parallelism} artifact.
 *
 * @param <I>
 *         input symbol class
 */
public class DFACacheOracle<I>
        implements DFALearningCacheOracle<I>, SupportsGrowingAlphabet<I>, Resumable<DFACacheOracleState<I>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DFACacheOracle.class);

    private IncrementalDFABuilder<I> incDfa;
    private final MembershipOracle<I, Boolean> delegate;

    DFACacheOracle(IncrementalDFABuilder<I> incDfa, MembershipOracle<I, Boolean> delegate) {
        this.incDfa = incDfa;
        this.delegate = delegate;
    }

    @Override
    public DFAEquivalenceOracle<I> createCacheConsistencyTest() {
        return new DFACacheConsistencyTest<>(incDfa);
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
        if (queries.isEmpty()) {
            return;
        }

        final Pair<Collection<ProxyQuery<I>>, Collection<Query<I, Boolean>>> cacheResult = queryCache(queries);
        final Collection<ProxyQuery<I>> unanswered = cacheResult.getFirst();
        final Collection<Query<I, Boolean>> duplicates = cacheResult.getSecond();

        delegate.processQueries(unanswered);
        updateCache(unanswered);

        if (!duplicates.isEmpty()) {
            queryCache(duplicates);
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
            LOGGER.warn(Category.DATASTRUCTURE,
                        "You currently plan to use a '{}', but the state contained a '{}'. This may yield unexpected behavior.",
                        thisClass,
                        stateClass);
        }

        this.incDfa = state.getBuilder();
    }

    Pair<Collection<ProxyQuery<I>>, Collection<Query<I, Boolean>>> queryCache(Collection<? extends Query<I, Boolean>> queries) {
        final List<ProxyQuery<I>> unanswered = new ArrayList<>();
        final List<Query<I, Boolean>> duplicates = new ArrayList<>();
        final Set<Word<I>> cache = new HashSet<>();

        for (Query<I, Boolean> q : queries) {
            final Word<I> input = q.getInput();
            final Acceptance acc = incDfa.lookup(input);
            if (acc == Acceptance.DONT_KNOW) {
                if (cache.add(input)) { // never seen before
                    unanswered.add(new ProxyQuery<>(q));
                } else {
                    duplicates.add(q);
                }
            } else {
                q.answer(acc.toBoolean());
            }
        }

        return Pair.of(unanswered, duplicates);
    }

    void updateCache(Collection<? extends ProxyQuery<I>> queries) {
        for (ProxyQuery<I> q : queries) {
            incDfa.insert(q.getInput(), q.getAnswer());
        }
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
