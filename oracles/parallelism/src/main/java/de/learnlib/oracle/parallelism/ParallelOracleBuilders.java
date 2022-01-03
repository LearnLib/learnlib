/* Copyright (C) 2013-2022 TU Dortmund
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
package de.learnlib.oracle.parallelism;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.learnlib.api.ObservableSUL;
import de.learnlib.api.SUL;
import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.OmegaMembershipOracle;
import de.learnlib.api.oracle.parallelism.ThreadPool.PoolPolicy;
import de.learnlib.oracle.membership.AbstractSULOmegaOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import net.automatalib.words.Word;

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
 *
 * @author Malte Isberner
 */
public final class ParallelOracleBuilders {

    private static final String FORKABLE_SUL_ERR = "SUL must be forkable for parallel processing";

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
        Preconditions.checkArgument(sul.canFork(), FORKABLE_SUL_ERR);
        return new DynamicParallelOracleBuilder<>(toSupplier(sul));
    }

    /**
     * Creates a {@link DynamicParallelOracleBuilder} using the provided {@code sul} as a supplier. This requires that
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
    public static <I, O> DynamicParallelOracleBuilder<I, Word<O>> newDynamicParallelOracle(StateLocalInputSUL<I, O> sul,
                                                                                           O undefinedInput) {
        Preconditions.checkArgument(sul.canFork(), FORKABLE_SUL_ERR);
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
        return newDynamicParallelOracle(Lists.asList(firstOracle, otherOracles));
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
     * Creates a {@link DynamicParallelOracleBuilder} using the provided {@code sul} as a supplier. This requires that
     * the sul is {@link SUL#canFork() forkable}.
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
        Preconditions.checkArgument(sul.canFork(), FORKABLE_SUL_ERR);
        // instantiate inner supplier to resolve generics
        return new DynamicParallelOmegaOracleBuilder<>(toSupplier(sul)::get);
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
        return newDynamicParallelOmegaOracle(Lists.asList(firstOracle, otherOracles));
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
        Preconditions.checkArgument(sul.canFork(), FORKABLE_SUL_ERR);
        return new StaticParallelOracleBuilder<>(toSupplier(sul));
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
        Preconditions.checkArgument(sul.canFork(), FORKABLE_SUL_ERR);
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
        return newStaticParallelOracle(Lists.asList(firstOracle, otherOracles));
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
        Preconditions.checkArgument(sul.canFork(), FORKABLE_SUL_ERR);
        // instantiate inner supplier to resolve generics
        return new StaticParallelOmegaOracleBuilder<>(toSupplier(sul)::get);
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
        return newStaticParallelOmegaOracle(Lists.asList(firstOracle, otherOracles));
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
}
