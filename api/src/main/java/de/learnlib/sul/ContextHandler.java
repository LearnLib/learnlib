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
package de.learnlib.sul;

/**
 * Facility for creating and disposing of contexts on which {@link ContextExecutableInput}s operate.
 * <p>
 * If used in a multi-threaded environment (e.g., {@link SUL#fork()}), an implementation of this interface must be
 * thread-safe, i.e., both the {@link #createContext()} and {@link #disposeContext(Object)} methods must be reentrant.
 * Furthermore, it must not make any assumptions as to the particular sequence in which these methods are called.
 *
 * @param <C>
 *         context type
 */
public interface ContextHandler<C> {

    C createContext();

    void disposeContext(C context);
}
