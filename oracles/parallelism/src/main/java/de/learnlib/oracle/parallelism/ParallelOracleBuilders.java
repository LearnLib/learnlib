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
package de.learnlib.oracle.parallelism;

import java.util.Collection;
import java.util.function.Supplier;

import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.OmegaMembershipOracle;
import de.learnlib.oracle.ThreadPool.PoolPolicy;
import de.learnlib.oracle.membership.AbstractSULOmegaOracle;
import de.learnlib.oracle.membership.SULAdaptiveOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.sul.ObservableSUL;
import de.learnlib.sul.SUL;
import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.word.Word;

/**
 * Builders for (static and dynamic) parallel oracles.
 * <p>
 * Using the methods defined in this class is the preferred way of instantiating parallel oracles.
 * <p>
 * <b>Usage examples</b>
 * <p>
 * Creating a static parallel oracle with a minimum batch size of 20 and a fixed thread pool, using two membership
 * oracles (running in two separate threads):
 * <pre>
 * ParallelOracleBuilders.newStaticParallelOracle(oracle1, oracle2)
 *      .withMinBatchSize(20)
 *      .create();
 * </pre>
 * <p>
 * Creating a dynamic parallel oracle with a custom executor, and a batch size of 5, using a shared membership oracle:
 * <pre>
 * ParallelOracleBuilders.newDynamicParallelOracle(() -&gt; membershipOracle)
 *      .withBatchSize(5)
 *      .withCustomExecutor(myExecutor)
 *      .create();
 * </pre>
 * <b>Note:</b> This requires the shared membership oracle to be thread-safe.
 * <p>
 * Creating a dynamic parallel oracle with a cached thread pool of maximum size 4, a batch size of 5, using a (forkabel)
 * SUL:
 * <pre>
 * ParallelOracleBuilders.newDynamicParallelOracle(sul)
 *      .withBatchSize(5)
 *      .withPoolSize(4)
 *      .withPoolPolicy(PoolPolicy.CACHED)
 *      .create();
 * </pre>
 */
public final class ParallelOracleBuilders {

    private ParallelOracleBuilders() {
        // prevent instantiation
    }

    /**
     * Creates a {@link DynamicParallelOracleBuilder} using the provided {@code sul} as a supplier. This requires that
     * the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> DynamicParallelOracleBuilder<I, Word<O>> newDynamicParallelOracle(SUL<I, O> sul) {
        checkFork(sul);
        return newDynamicParallelOracle(toSupplier(sul));
    }

    /**
     * Creates a {@link DynamicParallelOracleBuilder} using the provided {@code sul} as a supplier. This requires that
     * the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific membership oracle instances
     * @param undefinedInput
     *         the input symbol used for responding to inputs that are not
     *         {@link StateLocalInputSUL#currentlyEnabledInputs() enabled}.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> DynamicParallelOracleBuilder<I, Word<O>> newDynamicParallelOracle(StateLocalInputSUL<I, O> sul,
                                                                                           O undefinedInput) {
        checkFork(sul);
        return new DynamicParallelOracleBuilder<>(toSupplier(sul, undefinedInput));
    }

    /**
     * Creates a {@link DynamicParallelOracleBuilder} using the provided supplier.
     *
     * @param oracleSupplier
     *         the supplier for spawning new thread-specific membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, D> DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(Supplier<? extends MembershipOracle<I, D>> oracleSupplier) {
        return new DynamicParallelOracleBuilder<>(oracleSupplier);
    }

    /**
     * Convenience method for {@link #newDynamicParallelOracle(Collection)}.
     *
     * @param firstOracle
     *         the first (mandatory) oracle
     * @param otherOracles
     *         further (optional) oracles to be used by other threads
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    @SafeVarargs
    public static <I, D> DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(MembershipOracle<I, D> firstOracle,
                                                                                     MembershipOracle<I, D>... otherOracles) {
        return newDynamicParallelOracle(CollectionUtil.list(firstOracle, otherOracles));
    }

    /**
     * Creates a {@link DynamicParallelOracleBuilder} using the provided collection of membership oracles. The resulting
     * parallel oracle will always use a {@link PoolPolicy#FIXED} pool policy and spawn a separate thread for each of
     * the provided oracles (so that the oracles do not need to care about synchronization if they don't share state).
     *
     * @param oracles
     *         the oracle instances to distribute the queries to
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return the preconfigured oracle builder
     */
    public static <I, D> DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(Collection<? extends MembershipOracle<I, D>> oracles) {
        return new DynamicParallelOracleBuilder<>(oracles);
    }

    /**
     * Creates a {@link DynamicParallelOmegaOracleBuilder} using the provided {@code sul} as a supplier. This requires
     * that the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific omega membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> DynamicParallelOmegaOracleBuilder<?, I, Word<O>> newDynamicParallelOmegaOracle(ObservableSUL<?, I, O> sul) {
        checkFork(sul);
        return newDynamicParallelOmegaOracle(toSupplier(sul)::get);
    }

    /**
     * Creates a {@link DynamicParallelOmegaOracleBuilder} using the provided supplier.
     *
     * @param oracleSupplier
     *         the supplier for spawning new thread-specific membership oracle instances
     * @param <S>
     *         oracle state type
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <S, I, D> DynamicParallelOmegaOracleBuilder<S, I, D> newDynamicParallelOmegaOracle(Supplier<? extends OmegaMembershipOracle<S, I, D>> oracleSupplier) {
        return new DynamicParallelOmegaOracleBuilder<>(oracleSupplier);
    }

    /**
     * Convenience method for {@link #newDynamicParallelOmegaOracle(Collection)}.
     *
     * @param firstOracle
     *         the first (mandatory) oracle
     * @param otherOracles
     *         further (optional) oracles to be used by other threads
     * @param <S>
     *         oracle state type
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    @SafeVarargs
    public static <S, I, D> DynamicParallelOmegaOracleBuilder<S, I, D> newDynamicParallelOmegaOracle(
            OmegaMembershipOracle<S, I, D> firstOracle,
            OmegaMembershipOracle<S, I, D>... otherOracles) {
        return newDynamicParallelOmegaOracle(CollectionUtil.list(firstOracle, otherOracles));
    }

    /**
     * Creates a {@link DynamicParallelOmegaOracleBuilder} using the provided collection of membership oracles. The
     * resulting parallel oracle will always use a {@link PoolPolicy#FIXED} pool policy and spawn a separate thread for
     * each of the provided oracles (so that the oracles do not need to care about synchronization if they don't share
     * state).
     *
     * @param oracles
     *         the oracle instances to distribute the queries to
     * @param <S>
     *         oracle state type
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return the preconfigured oracle builder
     */
    public static <S, I, D> DynamicParallelOmegaOracleBuilder<S, I, D> newDynamicParallelOmegaOracle(Collection<? extends OmegaMembershipOracle<S, I, D>> oracles) {
        return new DynamicParallelOmegaOracleBuilder<>(oracles);
    }

    /**
     * Creates a {@link DynamicParallelAdaptiveOracleBuilder} using the provided {@code sul} as a supplier. This
     * requires that the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific omega membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> DynamicParallelAdaptiveOracleBuilder<I, O> newDynamicParallelAdaptiveOracle(SUL<I, O> sul) {
        checkFork(sul);
        return newDynamicParallelAdaptiveOracle(toAdaptiveSupplier(sul));
    }

    /**
     * Creates a {@link DynamicParallelAdaptiveOracleBuilder} using the provided supplier.
     *
     * @param oracleSupplier
     *         the supplier for spawning new thread-specific membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> DynamicParallelAdaptiveOracleBuilder<I, O> newDynamicParallelAdaptiveOracle(Supplier<? extends AdaptiveMembershipOracle<I, O>> oracleSupplier) {
        return new DynamicParallelAdaptiveOracleBuilder<>(oracleSupplier);
    }

    /**
     * Convenience method for {@link #newDynamicParallelAdaptiveOracle(Collection)}.
     *
     * @param firstOracle
     *         the first (mandatory) oracle
     * @param otherOracles
     *         further (optional) oracles to be used by other threads
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a preconfigured oracle builder
     */
    @SafeVarargs
    public static <I, O> DynamicParallelAdaptiveOracleBuilder<I, O> newDynamicParallelAdaptiveOracle(
            AdaptiveMembershipOracle<I, O> firstOracle,
            AdaptiveMembershipOracle<I, O>... otherOracles) {
        return newDynamicParallelAdaptiveOracle(CollectionUtil.list(firstOracle, otherOracles));
    }

    /**
     * Creates a {@link DynamicParallelAdaptiveOracleBuilder} using the provided collection of membership oracles. The
     * resulting parallel oracle will always use a {@link PoolPolicy#FIXED} pool policy and spawn a separate thread for
     * each of the provided oracles (so that the oracles do not need to care about synchronization if they don't share
     * state).
     *
     * @param oracles
     *         the oracle instances to distribute the queries to
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return the preconfigured oracle builder
     */
    public static <I, O> DynamicParallelAdaptiveOracleBuilder<I, O> newDynamicParallelAdaptiveOracle(Collection<? extends AdaptiveMembershipOracle<I, O>> oracles) {
        return new DynamicParallelAdaptiveOracleBuilder<>(oracles);
    }

    /**
     * Creates a {@link StaticParallelOracleBuilder} using the provided {@code sul} as a supplier. This requires that
     * the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> StaticParallelOracleBuilder<I, Word<O>> newStaticParallelOracle(SUL<I, O> sul) {
        checkFork(sul);
        return newStaticParallelOracle(toSupplier(sul));
    }

    /**
     * Creates a {@link StaticParallelOracleBuilder} using the provided {@code sul} as a supplier. This requires that
     * the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific membership oracle instances
     * @param undefinedInput
     *         the input symbol used for responding to inputs that are not {@link StateLocalInputSUL#currentlyEnabledInputs()
     *         enabled}.
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> StaticParallelOracleBuilder<I, Word<O>> newStaticParallelOracle(StateLocalInputSUL<I, O> sul,
                                                                                         O undefinedInput) {
        checkFork(sul);
        return new StaticParallelOracleBuilder<>(toSupplier(sul, undefinedInput));
    }

    /**
     * Creates a {@link StaticParallelOracleBuilder} using the provided supplier. Uses the further specified {@link
     * StaticParallelOracleBuilder#withPoolPolicy(PoolPolicy)} and {@link StaticParallelOracleBuilder#withNumInstances(int)}}
     * (or its defaults) to determine the thread pool.
     *
     * @param oracleSupplier
     *         the supplier for spawning new thread-specific membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(Supplier<? extends MembershipOracle<I, D>> oracleSupplier) {
        return new StaticParallelOracleBuilder<>(oracleSupplier);
    }

    /**
     * Convenience method for {@link #newStaticParallelOracle(Collection)}.
     *
     * @param firstOracle
     *         the first (mandatory) oracle
     * @param otherOracles
     *         further (optional) oracles to be used by other threads
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    @SafeVarargs
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(MembershipOracle<I, D> firstOracle,
                                                                                   MembershipOracle<I, D>... otherOracles) {
        return newStaticParallelOracle(CollectionUtil.list(firstOracle, otherOracles));
    }

    /**
     * Creates a {@link StaticParallelOracleBuilder} using the provided collection of membership oracles. The resulting
     * parallel oracle will always use a {@link PoolPolicy#FIXED} pool policy and spawn a separate thread for each of
     * the provided oracles (so that the oracles do not need to care about synchronization if they don't share state).
     *
     * @param oracles
     *         the oracle instances to distribute the queries to
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return the preconfigured oracle builder
     */
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(Collection<? extends MembershipOracle<I, D>> oracles) {
        return new StaticParallelOracleBuilder<>(oracles);
    }

    /**
     * Creates a {@link StaticParallelOmegaOracleBuilder} using the provided {@code sul} as a supplier. This requires
     * that the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific omega membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> StaticParallelOmegaOracleBuilder<?, I, Word<O>> newStaticParallelOmegaOracle(ObservableSUL<?, I, O> sul) {
        checkFork(sul);
        return newStaticParallelOmegaOracle(toSupplier(sul)::get);
    }

    /**
     * Creates a {@link StaticParallelOmegaOracleBuilder} using the provided supplier.
     *
     * @param oracleSupplier
     *         the supplier for spawning new thread-specific membership oracle instances
     * @param <S>
     *         oracle state type
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <S, I, D> StaticParallelOmegaOracleBuilder<S, I, D> newStaticParallelOmegaOracle(Supplier<? extends OmegaMembershipOracle<S, I, D>> oracleSupplier) {
        return new StaticParallelOmegaOracleBuilder<>(oracleSupplier);
    }

    /**
     * Convenience method for {@link #newStaticParallelOmegaOracle(Collection)}.
     *
     * @param firstOracle
     *         the first (mandatory) oracle
     * @param otherOracles
     *         further (optional) oracles to be used by other threads
     * @param <S>
     *         oracle state type
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    @SafeVarargs
    public static <S, I, D> StaticParallelOmegaOracleBuilder<S, I, D> newStaticParallelOmegaOracle(OmegaMembershipOracle<S, I, D> firstOracle,
                                                                                                   OmegaMembershipOracle<S, I, D>... otherOracles) {
        return newStaticParallelOmegaOracle(CollectionUtil.list(firstOracle, otherOracles));
    }

    /**
     * Creates a {@link StaticParallelOmegaOracleBuilder} using the provided collection of membership oracles. The
     * resulting parallel oracle will always use a {@link PoolPolicy#FIXED} pool policy and spawn a separate thread for
     * each of the provided oracles (so that the oracles do not need to care about synchronization if they don't share
     * state).
     *
     * @param oracles
     *         the oracle instances to distribute the queries to
     * @param <S>
     *         oracle state type
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     *
     * @return the preconfigured oracle builder
     */
    public static <S, I, D> StaticParallelOmegaOracleBuilder<S, I, D> newStaticParallelOmegaOracle(Collection<? extends OmegaMembershipOracle<S, I, D>> oracles) {
        return new StaticParallelOmegaOracleBuilder<>(oracles);
    }

    /**
     * Creates a {@link StaticParallelAdaptiveOracleBuilder} using the provided {@code sul} as a supplier. This requires
     * that the sul is {@link SUL#canFork() forkable}.
     *
     * @param sul
     *         the sul instance for spawning new thread-specific omega membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output domain type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> StaticParallelAdaptiveOracleBuilder<I, O> newStaticParallelAdaptiveOracle(SUL<I, O> sul) {
        checkFork(sul);
        return newStaticParallelAdaptiveOracle(toAdaptiveSupplier(sul));
    }

    /**
     * Creates a {@link StaticParallelAdaptiveOracleBuilder} using the provided supplier.
     *
     * @param oracleSupplier
     *         the supplier for spawning new thread-specific membership oracle instances
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a preconfigured oracle builder
     */
    public static <I, O> StaticParallelAdaptiveOracleBuilder<I, O> newStaticParallelAdaptiveOracle(Supplier<? extends AdaptiveMembershipOracle<I, O>> oracleSupplier) {
        return new StaticParallelAdaptiveOracleBuilder<>(oracleSupplier);
    }

    /**
     * Convenience method for {@link #newStaticParallelAdaptiveOracle(Collection)}.
     *
     * @param firstOracle
     *         the first (mandatory) oracle
     * @param otherOracles
     *         further (optional) oracles to be used by other threads
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a preconfigured oracle builder
     */
    @SafeVarargs
    public static <I, O> StaticParallelAdaptiveOracleBuilder<I, O> newStaticParallelAdaptiveOracle(
            AdaptiveMembershipOracle<I, O> firstOracle,
            AdaptiveMembershipOracle<I, O>... otherOracles) {
        return newStaticParallelAdaptiveOracle(CollectionUtil.list(firstOracle, otherOracles));
    }

    /**
     * Creates a {@link StaticParallelAdaptiveOracleBuilder} using the provided collection of membership oracles. The
     * resulting parallel oracle will always use a {@link PoolPolicy#FIXED} pool policy and spawn a separate thread for
     * each of the provided oracles (so that the oracles do not need to care about synchronization if they don't share
     * state).
     *
     * @param oracles
     *         the oracle instances to distribute the queries to
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return the preconfigured oracle builder
     */
    public static <I, O> StaticParallelAdaptiveOracleBuilder<I, O> newStaticParallelAdaptiveOracle(Collection<? extends AdaptiveMembershipOracle<I, O>> oracles) {
        return new StaticParallelAdaptiveOracleBuilder<>(oracles);
    }

    private static <I, O> Supplier<SULOracle<I, O>> toSupplier(SUL<I, O> sul) {
        return () -> new SULOracle<>(sul.fork());
    }

    private static <I, O> Supplier<StateLocalInputSULOracle<I, O>> toSupplier(StateLocalInputSUL<I, O> sul,
                                                                              O undefinedSymbol) {
        return () -> new StateLocalInputSULOracle<>(sul.fork(), undefinedSymbol);
    }

    private static <S, I, O> Supplier<OmegaMembershipOracle<?, I, Word<O>>> toSupplier(ObservableSUL<S, I, O> sul) {
        return () -> AbstractSULOmegaOracle.newOracle(sul.fork());
    }

    private static <I, O> Supplier<AdaptiveMembershipOracle<I, O>> toAdaptiveSupplier(SUL<I, O> sul) {
        return () -> new SULAdaptiveOracle<>(sul.fork());
    }

    private static <I, O> void checkFork(SUL<I, O> sul) {
        if (!sul.canFork()) {
            throw new IllegalArgumentException("SUL must be forkable for parallel processing");
        }
    }
}
