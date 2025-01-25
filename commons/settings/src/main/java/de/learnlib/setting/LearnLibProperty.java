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
package de.learnlib.setting;

import de.learnlib.oracle.ThreadPool.PoolPolicy;

/**
 * An enum of all the system properties currently used by LearnLib.
 */
public enum LearnLibProperty {

    /**
     * {@code learnlib.parallel.batch_size.dynamic}.
     * <p>
     * Size of query batches for dynamic parallel oracles.
     */
    PARALLEL_BATCH_SIZE_DYNAMIC("parallel.batch_size.dynamic"),

    /**
     * {@code learnlib.parallel.batch_size.static}.
     * <p>
     * Minimum size of query batches for static parallel oracles.
     */
    PARALLEL_BATCH_SIZE_STATIC("parallel.batch_size.static"),

    /**
     * {@code learnlib.parallel.pool_policy}.
     * <p>
     * Pool policy for threads of parallel oracles.
     *
     * @see PoolPolicy
     */
    PARALLEL_POOL_POLICY("parallel.pool_policy"),

    /**
     * {@code learnlib.parallel.pool_size}.
     * <p>
     * Size of thread pools for parallel oracles.
     */
    PARALLEL_POOL_SIZE("parallel.pool_size");

    private final String key;

    LearnLibProperty(String key) {
        this.key = "learnlib." + key;
    }

    /**
     * Returns the actual system property key of the property.
     *
     * @return the system property key of the property.
     */
    public String getPropertyKey() {
        return this.key;
    }
}
