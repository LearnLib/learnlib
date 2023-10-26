/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.filter.cache.moore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.parallelism.ParallelOracleBuilders;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.incremental.moore.IncrementalMooreBuilder;
import net.automatalib.incremental.moore.dag.IncrementalMooreDAGBuilder;
import net.automatalib.incremental.moore.tree.IncrementalMooreTreeBuilder;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A factory for creating thread-safe caches for {@link MooreMachine}-based {@link MembershipOracle}s. Parameters and
 * return types are tailored towards the use with {@link ParallelOracleBuilders}.
 */
public final class ThreadSafeMooreCaches {

    private ThreadSafeMooreCaches() {
        // prevent instantiation
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a Moore machine learning setup, using a DAG
     * for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a supplier for {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreDAGBuilder
     */
    public static <I, O> Supplier<ThreadSafeMooreCacheOracle<I, O>> createDAGCache(Alphabet<I> alphabet,
                                                                                   Supplier<? extends MembershipOracle<I, Word<O>>> oracleSupplier) {
        return createDAGCache(alphabet, null, oracleSupplier);
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a Moore machine learning setup, using a DAG
     * for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param errorSyms
     *         a mapping for defining a prefix-closure filter. If an output symbol has a non-null mapping all symbols
     *         <i>after</i> this symbol are replaced by the mapped value.
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a supplier for {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreDAGBuilder
     */
    public static <I, O> Supplier<ThreadSafeMooreCacheOracle<I, O>> createDAGCache(Alphabet<I> alphabet,
                                                                                   @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                                   Supplier<? extends MembershipOracle<I, Word<O>>> oracleSupplier) {
        // explicit type declaration is necessary to help the compiler
        final Function<Alphabet<I>, IncrementalMooreBuilder<I, O>> builder = IncrementalMooreDAGBuilder::new;
        return createSupplierBasedCache(alphabet, errorSyms, oracleSupplier, builder);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a Moore machine learning setup,
     * using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a collection of {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreDAGBuilder
     */
    public static <I, O> Collection<ThreadSafeMooreCacheOracle<I, O>> createDAGCache(Alphabet<I> alphabet,
                                                                                     Collection<? extends MembershipOracle<I, Word<O>>> oracles) {
        return createDAGCache(alphabet, null, oracles);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a Moore machine learning setup,
     * using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param errorSyms
     *         a mapping for defining a prefix-closure filter. If an output symbol has a non-null mapping all symbols
     *         <i>after</i> this symbol are replaced by the mapped value.
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a collection of {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreDAGBuilder
     */
    public static <I, O> Collection<ThreadSafeMooreCacheOracle<I, O>> createDAGCache(Alphabet<I> alphabet,
                                                                                     @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                                     Collection<? extends MembershipOracle<I, Word<O>>> oracles) {
        // explicit type declaration is necessary to help the compiler
        final Function<Alphabet<I>, IncrementalMooreBuilder<I, O>> builder = IncrementalMooreDAGBuilder::new;
        return createCollectionBasedCache(alphabet, errorSyms, oracles, builder);
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a Moore machine learning setup, using a
     * tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a supplier for {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreTreeBuilder
     */
    public static <I, O> Supplier<ThreadSafeMooreCacheOracle<I, O>> createTreeCache(Alphabet<I> alphabet,
                                                                                    Supplier<? extends MembershipOracle<I, Word<O>>> oracleSupplier) {
        return createTreeCache(alphabet, null, oracleSupplier);
    }

    /**
     * Enhances a given oracle supplier with a shared, thread-safe cache for a Moore machine learning setup, using a
     * tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param errorSyms
     *         a mapping for defining a prefix-closure filter. If an output symbol has a non-null mapping all symbols
     *         <i>after</i> this symbol are replaced by the mapped value.
     * @param oracleSupplier
     *         the supplier that is used to construct the delegate oracle for the cache, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a supplier for {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreTreeBuilder
     */
    public static <I, O> Supplier<ThreadSafeMooreCacheOracle<I, O>> createTreeCache(Alphabet<I> alphabet,
                                                                                    @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                                    Supplier<? extends MembershipOracle<I, Word<O>>> oracleSupplier) {
        // explicit type declaration is necessary to help the compiler
        final Function<Alphabet<I>, IncrementalMooreBuilder<I, O>> builder = IncrementalMooreTreeBuilder::new;
        return createSupplierBasedCache(alphabet, errorSyms, oracleSupplier, builder);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a Moore machine learning setup,
     * using a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a collection of {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreTreeBuilder
     */
    public static <I, O> Collection<ThreadSafeMooreCacheOracle<I, O>> createTreeCache(Alphabet<I> alphabet,
                                                                                      Collection<? extends MembershipOracle<I, Word<O>>> oracles) {
        return createTreeCache(alphabet, null, oracles);
    }

    /**
     * Enhances each oracle of a given collection with a shared, thread-safe cache for a Moore machine learning setup,
     * using a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param errorSyms
     *         a mapping for defining a prefix-closure filter. If an output symbol has a non-null mapping all symbols
     *         <i>after</i> this symbol are replaced by the mapped value.
     * @param oracles
     *         the collection of oracles which will be used as delegates (in case of a cache-miss) by the corresponding
     *         oracles caches.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a collection of {@link ThreadSafeMooreCacheOracle}s.
     *
     * @see IncrementalMooreTreeBuilder
     */
    public static <I, O> Collection<ThreadSafeMooreCacheOracle<I, O>> createTreeCache(Alphabet<I> alphabet,
                                                                                      @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                                      Collection<? extends MembershipOracle<I, Word<O>>> oracles) {
        // explicit type declaration is necessary to help the compiler
        final Function<Alphabet<I>, IncrementalMooreBuilder<I, O>> builder = IncrementalMooreTreeBuilder::new;
        return createCollectionBasedCache(alphabet, errorSyms, oracles, builder);
    }

    private static <I, O> Supplier<ThreadSafeMooreCacheOracle<I, O>> createSupplierBasedCache(Alphabet<I> alphabet,
                                                                                              @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                                              Supplier<? extends MembershipOracle<I, Word<O>>> oracleSupplier,
                                                                                              Function<? super Alphabet<I>, ? extends IncrementalMooreBuilder<I, O>> builder) {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final IncrementalMooreBuilder<I, O> incremental = builder.apply(alphabet);
        return () -> new ThreadSafeMooreCacheOracle<>(incremental, errorSyms, oracleSupplier.get(), lock);
    }

    private static <I, O> Collection<ThreadSafeMooreCacheOracle<I, O>> createCollectionBasedCache(Alphabet<I> alphabet,
                                                                                                  @Nullable Mapping<? super O, ? extends O> errorSyms,
                                                                                                  Collection<? extends MembershipOracle<I, Word<O>>> oracles,
                                                                                                  Function<? super Alphabet<I>, ? extends IncrementalMooreBuilder<I, O>> builder) {
        final ReadWriteLock lock = new ReentrantReadWriteLock();
        final IncrementalMooreBuilder<I, O> incremental = builder.apply(alphabet);
        final List<ThreadSafeMooreCacheOracle<I, O>> result = new ArrayList<>(oracles.size());

        for (MembershipOracle<I, Word<O>> oracle : oracles) {
            result.add(new ThreadSafeMooreCacheOracle<>(incremental, errorSyms, oracle, alphabet, lock));
        }

        return result;
    }

}
