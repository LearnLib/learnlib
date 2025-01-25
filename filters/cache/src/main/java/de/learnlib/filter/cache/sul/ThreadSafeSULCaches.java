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
 * A factory for creating thread-safe {@link SUL} caches.
 */
public final class ThreadSafeSULCaches {

    private ThreadSafeSULCaches() {
        // prevent instantiation
    }

    /**
     * Wraps a given {@link SUL} in a thread-safe variant of a {@link SULCache} that supports {@link SUL#fork() forking}
     * iff the given {@link SUL} supports it.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a DAG ({@link
     * #createDAGCache(Alphabet, SUL)}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param sul
     *         the supplier that is used as a delegate, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link ThreadSafeSULCache}.
     */
    public static <I, O> ThreadSafeSULCache<I, O> createCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return createDAGCache(alphabet, sul);
    }

    /**
     * Wraps a given {@link SUL} in a thread-safe variant of a {@link SULCache} that supports {@link SUL#fork() forking}
     * iff the given {@link SUL} supports it and uses a DAG for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param sul
     *         the supplier that is used as a delegate, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link ThreadSafeSULCache}.
     *
     * @see IncrementalMealyDAGBuilder
     */
    public static <I, O> ThreadSafeSULCache<I, O> createDAGCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return new ThreadSafeSULCache<>(new IncrementalMealyDAGBuilder<>(alphabet), sul);
    }

    /**
     * Wraps a given {@link SUL} in a thread-safe variant of a {@link SULCache} that supports {@link SUL#fork() forking}
     * iff the given {@link SUL} supports it and uses a tree for internal cache organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param sul
     *         the supplier that is used as a delegate, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link ThreadSafeSULCache}.
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> ThreadSafeSULCache<I, O> createTreeCache(Alphabet<I> alphabet, SUL<I, O> sul) {
        return new ThreadSafeSULCache<>(new IncrementalMealyTreeBuilder<>(alphabet), sul);
    }

    /**
     * Wraps a given {@link StateLocalInputSUL} in a thread-safe variant of a {@link StateLocalInputSULCache} that
     * supports {@link SUL#fork() forking} iff the given {@link SUL} supports it.
     * <p>
     * Note that this method does not specify the implementation to use for the cache. Currently, a tree ({@link
     * #createStateLocalInputTreeCache(Alphabet, StateLocalInputSUL)}) is used; however, this may change in the future.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param sul
     *         the supplier that is used as a delegate, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link ThreadSafeSULCache}.
     */
    public static <I, O> ThreadSafeStateLocalInputSULCache<I, O> createStateLocalInputCache(Alphabet<I> alphabet,
                                                                                            StateLocalInputSUL<I, O> sul) {
        return createStateLocalInputTreeCache(alphabet, sul);
    }

    /**
     * Wraps a given {@link StateLocalInputSUL} in a thread-safe variant of a {@link StateLocalInputSULCache} that
     * supports {@link SUL#fork() forking} iff the given {@link SUL} supports it and uses a tree for internal cache
     * organization.
     *
     * @param alphabet
     *         the alphabet containing the symbols of possible queries
     * @param sul
     *         the supplier that is used as a delegate, in case of a cache-miss.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a {@link ThreadSafeStateLocalInputSULCache}.
     *
     * @see IncrementalMealyTreeBuilder
     */
    public static <I, O> ThreadSafeStateLocalInputSULCache<I, O> createStateLocalInputTreeCache(Alphabet<I> alphabet,
                                                                                                StateLocalInputSUL<I, O> sul) {
        return new ThreadSafeStateLocalInputSULCache<>(new IncrementalMealyTreeBuilder<>(alphabet), sul);
    }
}
