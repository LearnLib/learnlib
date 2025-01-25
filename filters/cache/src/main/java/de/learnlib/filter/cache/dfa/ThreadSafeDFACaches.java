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
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;

/**
 * A factory for creating thread-safe caches for {@link DFA}-based {@link MembershipOracle}s. Parameters and return
 * types are tailored towards the use with {@link ParallelOracleBuilders}.
 */
public final class ThreadSafeDFACaches {

    private ThreadSafeDFACaches() {
        // prevent instantiation
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a DFA learning setup.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link
     * #createDAGCache(Alphabet, Supplier)}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return a supplier for {@link ThreadSafeDFACacheOracle}s.
     */
    public static <I> Supplier<ThreadSafeDFACacheOracle<I>> createCache(Alphabet<I> alphabet,
                                                                        Supplier<? extends MembershipOracle<I, Boolean>> oracleSupplier) {
        return createDAGCache(alphabet, oracleSupplier);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a DFA learning setup.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link
     * #createDAGCache(Alphabet, Collection)}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     *
     * @return a collection of {@link ThreadSafeDFACacheOracle}s.
     */
    public static <I> Collection<ThreadSafeDFACacheOracle<I>> createCache(Alphabet<I> alphabet,
                                                                          Collection<? extends MembershipOracle<I, Boolean>> oracles) {
        return createDAGCache(alphabet, oracles);
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a DFA learning setup, using a DAG for
     * internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return a supplier for {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalDFADAGBuilder
     */
    public static <I> Supplier<ThreadSafeDFACacheOracle<I>> createDAGCache(Alphabet<I> alphabet,
                                                                           Supplier<? extends MembershipOracle<I, Boolean>> oracleSupplier) {
        return createSupplierBasedCache(alphabet, oracleSupplier, IncrementalDFADAGBuilder::new);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a DFA learning setup, using a DAG
     * for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     *
     * @return a collection of {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalDFADAGBuilder
     */
    public static <I> Collection<ThreadSafeDFACacheOracle<I>> createDAGCache(Alphabet<I> alphabet,
                                                                             Collection<? extends MembershipOracle<I, Boolean>> oracles) {
        return createCollectionBasedCache(alphabet, oracles, IncrementalDFADAGBuilder::new);
    }

    /**
     * The prefix-closed version of {@link #createDAGCache(Alphabet, Supplier)}.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return a supplier for {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalPCDFADAGBuilder
     */
    public static <I> Supplier<ThreadSafeDFACacheOracle<I>> createDAGPCCache(Alphabet<I> alphabet,
                                                                             Supplier<? extends MembershipOracle<I, Boolean>> oracleSupplier) {
        return createSupplierBasedCache(alphabet, oracleSupplier, IncrementalPCDFADAGBuilder::new);
    }

    /**
     * The prefix-closed version of {@link #createDAGCache(Alphabet, Collection)}.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     *
     * @return a collection of {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalPCDFADAGBuilder
     */
    public static <I> Collection<ThreadSafeDFACacheOracle<I>> createDAGPCCache(Alphabet<I> alphabet,
                                                                               Collection<? extends MembershipOracle<I, Boolean>> oracles) {
        return createCollectionBasedCache(alphabet, oracles, IncrementalPCDFADAGBuilder::new);
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a DFA learning setup, using a tree for
     * internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return a supplier for {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalDFATreeBuilder
     */
    public static <I> Supplier<ThreadSafeDFACacheOracle<I>> createTreeCache(Alphabet<I> alphabet,
                                                                            Supplier<? extends MembershipOracle<I, Boolean>> oracleSupplier) {
        return createSupplierBasedCache(alphabet, oracleSupplier, IncrementalDFATreeBuilder::new);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a DFA learning setup, using a
     * tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     *
     * @return a collection of {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalDFATreeBuilder
     */
    public static <I> Collection<ThreadSafeDFACacheOracle<I>> createTreeCache(Alphabet<I> alphabet,
                                                                              Collection<? extends MembershipOracle<I, Boolean>> oracles) {
        return createCollectionBasedCache(alphabet, oracles, IncrementalDFATreeBuilder::new);
    }

    /**
     * Prefix-closed version of {@link #createTreeCache(Alphabet, Supplier)}.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return a supplier for {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalPCDFATreeBuilder
     */
    public static <I> Supplier<ThreadSafeDFACacheOracle<I>> createTreePCCache(Alphabet<I> alphabet,
                                                                              Supplier<? extends MembershipOracle<I, Boolean>> oracleSupplier) {
        return createSupplierBasedCache(alphabet, oracleSupplier, IncrementalPCDFATreeBuilder::new);
    }

    /**
     * Prefix-closed version of {@link #createTreeCache(Alphabet, Collection)}.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     *
     * @return a supplier for {@link ThreadSafeDFACacheOracle}s.
     *
     * @see IncrementalPCDFATreeBuilder
     */
    public static <I> Collection<ThreadSafeDFACacheOracle<I>> createTreePCCache(Alphabet<I> alphabet,
                                                                                Collection<? extends MembershipOracle<I, Boolean>> oracles) {
        return createCollectionBasedCache(alphabet, oracles, IncrementalPCDFATreeBuilder::new);
    }

    private static <I> Supplier<ThreadSafeDFACacheOracle<I>> createSupplierBasedCache(Alphabet<I> alphabet,
                                                                                      Supplier<? extends MembershipOracle<I, Boolean>> oracleSupplier,
                                                                                      Function<? super Alphabet<I>, ? extends IncrementalDFABuilder<I>> builder) {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final IncrementalDFABuilder<I> incremental = builder.apply(alphabet);
        return () -> new ThreadSafeDFACacheOracle<>(incremental, oracleSupplier.get(), lock);
    }

    private static <I> Collection<ThreadSafeDFACacheOracle<I>> createCollectionBasedCache(Alphabet<I> alphabet,
                                                                                          Collection<? extends MembershipOracle<I, Boolean>> oracles,
                                                                                          Function<? super Alphabet<I>, ? extends IncrementalDFABuilder<I>> builder) {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final IncrementalDFABuilder<I> incremental = builder.apply(alphabet);
        final List<ThreadSafeDFACacheOracle<I>> result = new ArrayList<>(oracles.size());

        for (MembershipOracle<I, Boolean> oracle : oracles) {
            result.add(new ThreadSafeDFACacheOracle<>(incremental, oracle, lock));
        }

        return result;
    }
}
