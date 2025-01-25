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
package de.learnlib.filter.reuse;

import java.util.Collections;
import java.util.Set;

import de.learnlib.filter.reuse.tree.BoundedDeque.AccessPolicy;
import de.learnlib.filter.reuse.tree.BoundedDeque.EvictPolicy;
import de.learnlib.filter.reuse.tree.ReuseTreeBuilder;
import de.learnlib.filter.reuse.tree.SystemStateHandler;

/**
 * Default values for {@link ReuseTreeBuilder} and {@link ReuseOracleBuilder}.
 */
public final class BuilderDefaults {

    private BuilderDefaults() {
        // prevent instantiation
    }

    public static boolean enabledSystemStateInvalidation() {
        return true;
    }

    public static <S> SystemStateHandler<S> systemStateHandler() {
        return state -> {};
    }

    public static <I> Set<I> invariantInputs() {
        return Collections.emptySet();
    }

    public static <O> Set<O> failureOutputs() {
        return Collections.emptySet();
    }

    public static int maxSystemStates() {
        return -1;
    }

    public static AccessPolicy accessPolicy() {
        return AccessPolicy.LIFO;
    }

    public static EvictPolicy evictPolicy() {
        return EvictPolicy.EVICT_OLDEST;
    }
}
