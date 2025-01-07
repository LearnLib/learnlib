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
package de.learnlib.filter.cache.sul;

import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;

/**
 * A factory for creating caches for {@link SUL}s.
 */
public final class SULCaches {

    private SULCaches() {
        // prevent instantiation
    }

    /**
     * Creates a {@link SULCache} for a given {@link SUL}.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link
     * #createDAGCache}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the input alphabet
     * @param sul
     *         the sul
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link SULCache} with a default implementation
     */
    public static <I, O> SULCache<I, O> createCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return createDAGCache(alphabet, sul);
    }

    /**
     * Creates a {@link SULCache} for a given {@link SUL}, using a DAG for internal cache organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param sul
     *         the sul
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link SULCache} with a DAG-based implementation
     *
     * @see IncrementalMealyDAGBuilder
     */
    public static <I, O> SULCache<I, O> createDAGCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return new SULCache<>(new IncrementalMealyDAGBuilder<>(alphabet), sul);
    }

    /**
     * Creates a {@link SULCache} for a given {@link SUL}, using a tree for internal cache organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param sul
     *         the sul
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link SULCache} with a tree-based implementation
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> SULCache<I, O> createTreeCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return new SULCache<>(new IncrementalMealyTreeBuilder<>(alphabet), sul);
    }

    /**
     * Creates a {@link StateLocalInputSULCache} for a given {@link StateLocalInputSUL}.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a tree ({@link
     * #createStateLocalInputTreeCache}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the input alphabet
     * @param sul
     *         the sul
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link StateLocalInputSULCache} with a default implementation
     */
    public static <I, O> StateLocalInputSULCache<I, O> createStateLocalInputCache(Alphabet<I> alphabet,
                                                                                  StateLocalInputSUL<I, O> sul) {
        return createStateLocalInputTreeCache(alphabet, sul);
    }

    /**
     * Creates a {@link StateLocalInputSULCache} for a given {@link StateLocalInputSUL}, using a tree for internal cache
     * organization.
     *
     * @param alphabet
     *         the input alphabet
     * @param sul
     *         the sul
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link StateLocalInputSULCache} with a tree-based implementation
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> StateLocalInputSULCache<I, O> createStateLocalInputTreeCache(Alphabet<I> alphabet,
                                                                                      StateLocalInputSUL<I, O> sul) {
        return new StateLocalInputSULCache<>(new IncrementalMealyTreeBuilder<>(alphabet), sul);
    }
}
