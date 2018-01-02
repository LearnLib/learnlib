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
 * Abstract base class for jobs (i.e., {@link Runnable}s) that process queries.
 * <p>
 * Subclasses specify how the delegate oracle is obtained.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
@ParametersAreNonnullByDefault
abstract class AbstractQueriesJob<I, D> implements Runnable {

    private final Collection<? extends Query<I, D>> queries;

    AbstractQueriesJob(Collection<? extends Query<I, D>> queries) {
        this.queries = queries;
    }

    @Override
    public void run() {
        MembershipOracle<I, D> oracle = getOracle();

        oracle.processQueries(queries);
    }

    @Nonnull
    protected abstract MembershipOracle<I, D> getOracle();
}
