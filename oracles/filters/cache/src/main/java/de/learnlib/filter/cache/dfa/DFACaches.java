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

import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.incremental.dfa.IncrementalDFABuilder;
import net.automatalib.incremental.dfa.dag.IncrementalDFADAGBuilder;
import net.automatalib.incremental.dfa.dag.IncrementalPCDFADAGBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.incremental.dfa.tree.IncrementalPCDFATreeBuilder;
import net.automatalib.words.Alphabet;

public final class DFACaches {

    private DFACaches() {
        throw new IllegalStateException("Constructor should never be invoked");
    }

    /**
     * Creates a prefix-closed cache oracle for a DFA learning setup, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param mqOracle
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalPCDFADAGBuilder
     */
    public static <I> DFACacheOracle<I> createDAGPCCache(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqOracle) {
        return DFACacheOracle.createDAGPCCacheOracle(alphabet, mqOracle);
    }

    /**
     * Creates a cache oracle for a DFA learning setup, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param mqOracle
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalDFATreeBuilder
     */
    public static <I> DFACacheOracle<I> createTreeCache(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqOracle) {
        return DFACacheOracle.createTreeCacheOracle(alphabet, mqOracle);
    }

    /**
     * Creates a prefix-closed cache oracle for a DFA learning setup, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param mqOracle
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalPCDFATreeBuilder
     */
    public static <I> DFACacheOracle<I> createTreePCCache(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqOracle) {
        return DFACacheOracle.createTreePCCacheOracle(alphabet, mqOracle);
    }

    public static <I> DFAHashCacheOracle<I> createHashCache(MembershipOracle<I, Boolean> mqOracle) {
        return new DFAHashCacheOracle<>(mqOracle);
    }

    /**
     * Creates a cache oracle for a DFA learning setup.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link
     * IncrementalDFABuilder}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     *
     * @return a Mealy learning cache with a default implementation
     */
    public static <I> DFACacheOracle<I> createCache(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqOracle) {
        return createDAGCache(alphabet, mqOracle);
    }

    /**
     * Creates a cache oracle for a DFA learning setup, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param mqOracle
     *         the oracle to delegate queries to, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     *
     * @return the cached {@link DFACacheOracle}.
     *
     * @see IncrementalDFADAGBuilder
     */
    public static <I> DFACacheOracle<I> createDAGCache(Alphabet<I> alphabet, MembershipOracle<I, Boolean> mqOracle) {
        return DFACacheOracle.createDAGCacheOracle(alphabet, mqOracle);
    }
}
