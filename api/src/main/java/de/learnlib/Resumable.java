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
package de.learnlib;

import java.io.Serializable;

/**
 * Data structures that implement this interface can be "suspended" by means of exposing a state object that contains
 * all data that is necessary to resume from this state at a later point in time.
 * <p>
 * <b>Note:</b> Objects returned by this interface are not guaranteed to be binary compatible across versions (like a
 * {@link Serializable} object). Only use them for short-lived scopes in the same learning setup. For persistent storage
 * of e.g. hypotheses models, use the model serializers of AutomataLib.
 *
 * @param <T>
 *         The type of the learner state.
 */
public interface Resumable<T> {

    /**
     * Expose the state object.
     *
     * @return The state.
     */
    T suspend();

    /**
     * Resume the datastructure from a previously suspended point in time.
     *
     * @param state
     *         The learner state.
     */
    void resume(T state);
}
