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

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;

/**
 * A queries job that maintains a thread-local reference to a membership oracle, and dynamically selects that oracle
 * depending on the executing thread.
 * <p>
 * Note: This class assumes that the respective {@link ThreadLocal#get()} methods never returns a {@code null}
 * reference.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
final class DynamicQueriesJob<I, D> extends AbstractQueriesJob<I, D> {

    @Nonnull
    private final ThreadLocal<? extends MembershipOracle<I, D>> threadLocalOracle;

    DynamicQueriesJob(Collection<? extends Query<I, D>> queries,
                      ThreadLocal<? extends MembershipOracle<I, D>> threadLocalOracle) {
        super(queries);
        this.threadLocalOracle = threadLocalOracle;
    }

    @Override
    protected MembershipOracle<I, D> getOracle() {
        return threadLocalOracle.get();
    }

}
