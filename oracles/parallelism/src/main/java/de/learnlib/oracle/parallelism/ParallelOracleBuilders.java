/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Suppliers;
import de.learnlib.api.oracle.MembershipOracle;

/**
 * Builders for (static and dynamic) parallel oracles.
 * <p>
 * Using the methods defined in this class is the preferred way of instantiating parallel oracles.
 * <p>
 * <b>Usage examples</b>
 * <p>
 * Creating a static parallel oracle with a minimum batch size of 20 and a fixed thread pool, using a membership oracle
 * shared by 4 threads:
 * <pre>
 * ParallelOracleBuilders.newStaticParallelOracle(membershipOracle)
 *      .withMinBatchSize(20)
 *      .withNumInstances(4)
 *      .withPoolPolicy(PoolPolicy.FIXED)
 *      .create();
 * </pre>
 * <p>
 * Creating a dynamic parallel oracle with a custom executor, and a batch size of 5, using a shared membership oracle:
 * <pre>
 * ParallelOracleBuilders.newDynamicParallelOracle(membershipOracle)
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

    @Nonnull
    public static <I, D> DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(MembershipOracle<I, D> sharedOracle) {
        return newDynamicParallelOracle(() -> sharedOracle);
    }

    @Nonnull
    public static <I, D> DynamicParallelOracleBuilder<I, D> newDynamicParallelOracle(Supplier<? extends MembershipOracle<I, D>> oracleSupplier) {
        return new DynamicParallelOracleBuilder<>(oracleSupplier);
    }

    @Nonnull
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(MembershipOracle<I, D> sharedOracle) {
        return newStaticParallelOracle(Suppliers.ofInstance(sharedOracle));
    }

    @Nonnull
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(Supplier<? extends MembershipOracle<I, D>> oracleSupplier) {
        return new StaticParallelOracleBuilder<>(oracleSupplier);
    }

    @Nonnull
    @SafeVarargs
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(MembershipOracle<I, D> firstOracle,
                                                                                   MembershipOracle<I, D>... otherOracles) {
        List<MembershipOracle<I, D>> oracles = new ArrayList<>(otherOracles.length + 1);
        oracles.add(firstOracle);
        Collections.addAll(oracles, otherOracles);
        return newStaticParallelOracle(oracles);
    }

    @Nonnull
    public static <I, D> StaticParallelOracleBuilder<I, D> newStaticParallelOracle(Collection<? extends MembershipOracle<I, D>> oracles) {
        return new StaticParallelOracleBuilder<>(oracles);
    }
}
