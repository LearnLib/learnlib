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
package de.learnlib.filter.cache.mealy;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.DynamicIncrementalMealyTreeBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.word.Word;

/**
 * A factory for creating caches for mealy-based {@link MembershipOracle}s.
 */
public final class MealyCaches {

    private MealyCaches() {
        // prevent instantiation
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link
     * #createDAGCache}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a default implementation
     */
    public static <I, O> MealyCacheOracle<I, O> createCache(Alphabet<I> alphabet,
                                                            MembershipOracle<I, Word<O>> mqOracle) {
        return createDAGCache(alphabet, mqOracle);
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a DAG-based implementation
     *
     * @see IncrementalMealyDAGBuilder
     */
    public static <I, O> MealyCacheOracle<I, O> createDAGCache(Alphabet<I> alphabet,
                                                               MembershipOracle<I, Word<O>> mqOracle) {
        return new MealyCacheOracle<>(new IncrementalMealyDAGBuilder<>(alphabet), null, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param errorSyms
     *         a mapping for the prefix-closure filter
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a DAG-based implementation
     *
     * @see IncrementalMealyDAGBuilder
     */
    public static <I, O> MealyCacheOracle<I, O> createDAGCache(Alphabet<I> alphabet,
                                                               Mapping<? super O, ? extends O> errorSyms,
                                                               MembershipOracle<I, Word<O>> mqOracle) {
        return new MealyCacheOracle<>(new IncrementalMealyDAGBuilder<>(alphabet), errorSyms, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a tree-based implementation
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> MealyCacheOracle<I, O> createTreeCache(Alphabet<I> alphabet,
                                                                MembershipOracle<I, Word<O>> mqOracle) {
        return new MealyCacheOracle<>(new IncrementalMealyTreeBuilder<>(alphabet), null, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param errorSyms
     *         a mapping for the prefix-closure filter
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a tree-based implementation
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> MealyCacheOracle<I, O> createTreeCache(Alphabet<I> alphabet,
                                                                Mapping<? super O, ? extends O> errorSyms,
                                                                MembershipOracle<I, Word<O>> mqOracle) {
        return new MealyCacheOracle<>(new IncrementalMealyTreeBuilder<>(alphabet), errorSyms, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup with a dynamic alphabet storage, using a tree for
     * internal cache organization.
     * <p>
     * Note: Due to the dynamic alphabet storage, memory consumption of a dense tree may be higher than normal caches
     * with a predefined alphabet. However, for sparse data, the memory consumption may be lower than only memory for
     * the actual data of the tree is allocated.
     *
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a tree-based implementation
     *
     * @see DynamicIncrementalMealyTreeBuilder
     */
    public static <I, O> MealyCacheOracle<I, O> createDynamicTreeCache(MembershipOracle<I, Word<O>> mqOracle) {
        return new MealyCacheOracle<>(new DynamicIncrementalMealyTreeBuilder<>(), null, mqOracle);
    }

    /**
     * Creates a cache oracle for a Mealy machine learning setup with a dynamic alphabet storage, using a tree for
     * internal cache organization.
     * <p>
     * Note: Due to the dynamic alphabet storage, memory consumption of a dense tree may be higher than normal caches
     * with a predefined alphabet. However, for sparse data, the memory consumption may be lower than only memory for
     * the actual data of the tree is allocated.
     *
     * @param errorSyms
     *         a mapping for the prefix-closure filter
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a Mealy learning cache with a tree-based implementation
     *
     * @see DynamicIncrementalMealyTreeBuilder
     */
    public static <I, O> MealyCacheOracle<I, O> createDynamicTreeCache(Mapping<? super O, ? extends O> errorSyms,
                                                                       MembershipOracle<I, Word<O>> mqOracle) {
        return new MealyCacheOracle<>(new DynamicIncrementalMealyTreeBuilder<>(), errorSyms, mqOracle);
    }

    /**
     * Creates a cache oracle for an adaptive Mealy machine learning setup, using a tree for internal cache
     * organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a symbol-based Mealy learning cache with a tree-based implementation
     */
    public static <I, O> AdaptiveQueryCache<I, O> createAdaptiveQueryCache(Alphabet<I> alphabet,
                                                                           AdaptiveMembershipOracle<I, O> mqOracle) {
        return new AdaptiveQueryCache<>(mqOracle, alphabet);
    }
}
