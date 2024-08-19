/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.oracle;

import java.util.Collection;
import java.util.Collections;

import de.learnlib.query.AdaptiveQuery;

public interface AdaptiveMembershipOracle<I, O> extends BatchProcessor<AdaptiveQuery<I, O>> {

    void processQueries(Collection<? extends AdaptiveQuery<I, O>> queries);

    default void processQuery(AdaptiveQuery<I, O> query) {
        processQueries(Collections.singleton(query));
    }

    @Override
    default void processBatch(Collection<? extends AdaptiveQuery<I, O>> batch) {
        processQueries(batch);
    }
}

