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
package de.learnlib.api.algorithm.feature;

import java.io.Serializable;

/**
 * Learning algorithms that implement this interface can be "suspended" by means of exposing a serializable state object
 * that contains all data that is necessary to resume learning process from a previous state.
 *
 * @param <T>
 *         The type of the serializable learner state.
 *
 * @author bainczyk
 */
public interface ResumableLearner<T extends Serializable> {

    /**
     * Expose the serializable learner state object.
     * <p>
     * Does not stop a running learning process. Since most data structures that are used during learning are mutable,
     * use this method inside of a learning loop with care.
     *
     * @return The learner state.
     */
    T suspend();

    /**
     * Does not get the learner to continue learning. Instead, the learner updates its internal state according to the
     * given state object.
     *
     * @param state
     *         The learner state.
     */
    void resume(T state);
}
