/* Copyright (C) 2013-2019 TU Dortmund
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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Lists;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.oracle.parallelism.ParallelOracle.PoolPolicy;

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
 * <p>
 * Creating a dynamic parallel oracle with a cached thread pool of maximum size 4, a batch size of 5, using an oracle
 * supplier:
 * <pre>
 * ParallelOracleBuilders.newDynamicParallelOracle(oracleSupplier)
 *      .withBatchSize(5)
 *      .withPoolSize(4)
 *      .withPoolPolicy(PoolPolicy.CACHED)
 *      .create();
 * </pre>
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
public final class ParallelOracleBuilders {

    private ParallelOracleBuilders() {
        throw new AssertionError("Constructor should not be invoked");
    }

    /**
     * Creates a {@link DynamicParallelOracleBuilder} using the provided supplier. Uses the further specified
     * {@link DynamicParallelOracleBuilder#withPoolPolicy(PoolPolicy)} and
     * {@link DynamicParallelOracleBuilder#withPoolSize(int)} (or its defaults) to determine the thread pool.
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
    @Nonnull
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
    @Nonnull
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
    @Nonnull
    public static <I, D> DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(Collection<? extends MembershipOracle<I, D>> oracles) {
        return new DynamicParallelOracleBuilder<>(oracles);
    }

    /**
     * Creates a {@link StaticParallelOracleBuilder} using the provided supplier. Uses the further specified
     * {@link StaticParallelOracleBuilder#withPoolPolicy(PoolPolicy)} and
     * {@link StaticParallelOracleBuilder#withNumInstances(int)}} (or its defaults) to determine the thread pool.
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
    @Nonnull
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
    @Nonnull
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
    @Nonnull
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(Collection<? extends MembershipOracle<I, D>> oracles) {
        return new StaticParallelOracleBuilder<>(oracles);
    }
}
