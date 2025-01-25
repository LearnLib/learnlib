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
package de.learnlib.filter.cache.moore;

import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.mapping.Mapping;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.incremental.moore.dag.IncrementalMooreDAGBuilder;
import net.automatalib.incremental.moore.tree.IncrementalMooreTreeBuilder;
import net.automatalib.word.Word;

/**
 * A factory for creating caches for Moore-based {@link MembershipOracle}s.
 */
public final class MooreCaches {

    private MooreCaches() {
        // prevent instantiation
    }

    /**
     * Creates a cache oracle for a Moore machine learning setup.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG
     * ({@link #createDAGCache}) is used; however, this may change in the future.
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
     * @return a Moore learning cache with a default implementation
     */
    public static <I, O> MooreCacheOracle<I, O> createCache(Alphabet<I> alphabet,
                                                            MembershipOracle<I, Word<O>> mqOracle) {
        return createDAGCache(alphabet, mqOracle);
    }

    /**
     * Creates a cache oracle for a Moore machine learning setup, using a DAG for internal cache organization.
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
     * @return a Moore learning cache with a DAG-based implementation
     *
     * @see IncrementalMooreDAGBuilder
     */
    public static <I, O> MooreCacheOracle<I, O> createDAGCache(Alphabet<I> alphabet,
                                                               MembershipOracle<I, Word<O>> mqOracle) {
        return new MooreCacheOracle<>(new IncrementalMooreDAGBuilder<>(alphabet), null, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Moore machine learning setup, using a DAG for internal cache organization.
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
     * @return a Moore learning cache with a DAG-based implementation
     *
     * @see IncrementalMooreDAGBuilder
     */
    public static <I, O> MooreCacheOracle<I, O> createDAGCache(Alphabet<I> alphabet,
                                                               Mapping<? super O, ? extends O> errorSyms,
                                                               MembershipOracle<I, Word<O>> mqOracle) {
        return new MooreCacheOracle<>(new IncrementalMooreDAGBuilder<>(alphabet), errorSyms, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Moore machine learning setup, using a tree for internal cache organization.
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
     * @return a Moore learning cache with a tree-based implementation
     *
     * @see IncrementalMooreTreeBuilder
     */
    public static <I, O> MooreCacheOracle<I, O> createTreeCache(Alphabet<I> alphabet,
                                                                MembershipOracle<I, Word<O>> mqOracle) {
        return new MooreCacheOracle<>(new IncrementalMooreTreeBuilder<>(alphabet), null, mqOracle, alphabet);
    }

    /**
     * Creates a cache oracle for a Moore machine learning setup, using a tree for internal cache organization.
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
     * @return a Moore learning cache with a tree-based implementation
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> MooreCacheOracle<I, O> createTreeCache(Alphabet<I> alphabet,
                                                                Mapping<? super O, ? extends O> errorSyms,
                                                                MembershipOracle<I, Word<O>> mqOracle) {
        return new MooreCacheOracle<>(new IncrementalMooreTreeBuilder<>(alphabet), errorSyms, mqOracle, alphabet);
    }

}
