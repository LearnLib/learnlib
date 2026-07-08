/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib;

import de.learnlib.algorithm.LearningAlgorithm;

/**
 * A utility interface for managing the learner state as required by the {@link LearningAlgorithm} contract. Given an
 * implementation of {@link #hasLearningProcessStarted()}, this interface provides default implementations for requiring
 * whether the learning process has started yet.
 */
@FunctionalInterface
public interface LearnerStateTracker {

    /**
     * Returns whether the learning process has started yet.
     *
     * @return {@code true} if the learning process has started, {@code false} otherwise
     */
    boolean hasLearningProcessStarted();

    /**
     * Requires that the learning process has not yet started.
     *
     * @throws IllegalStateException
     *         if {@link #hasLearningProcessStarted()} returns {@code true}
     */
    default void requireLearningProcessNotStarted() {
        if (hasLearningProcessStarted()) {
            throw new IllegalStateException("Learning process has already been started");
        }
    }

    /**
     * Requires that the learning process has started.
     *
     * @throws IllegalArgumentException
     *         if {@link #hasLearningProcessStarted()} returns {@code false}
     */
    default void requireLearningProcessStarted() {
        if (!hasLearningProcessStarted()) {
            throw new IllegalStateException("Learning process has not been started yet");
        }
    }
}
