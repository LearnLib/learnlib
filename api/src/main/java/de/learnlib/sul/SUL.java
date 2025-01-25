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

import de.learnlib.exception.SULException;

/**
 * Interface for a system under learning (SUL) that can make single steps.
 *
 * @param <I>
 *         input symbols
 * @param <O>
 *         output symbols
 */
public interface SUL<I, O> {

    /**
     * setup SUL.
     */
    void pre();

    /**
     * shut down SUL.
     */
    void post();

    /**
     * make one step on the SUL.
     *
     * @param in
     *         input to the SUL
     *
     * @return output of SUL
     *
     * @throws SULException
     *         if the input symbol cannot be executed on the SUL
     */
    O step(I in);

    /**
     * Returns whether this SUL is capable of {@link #fork() forking}.
     *
     * @return {@code true} if this SUL can be forked, {@code false} otherwise
     *
     * @see #fork()
     */
    default boolean canFork() {
        return false;
    }

    /**
     * Forks this SUL, if possible. The fork of a SUL is a copy which behaves exactly the same as this SUL. This method
     * should always return a reseted SUL, regardless of whether this call is made between a call to {@link #pre()} and
     * {@link #post()}.
     * <p>
     * If {@link #canFork()} returns {@code true}, this method must return a non-{@code null} object, which should
     * behave exactly like this SUL (in particular, it must be forkable as well). Otherwise, a {@link
     * UnsupportedOperationException} must be thrown.
     * <p>
     * Implementation note: if resetting a SUL changes the internal state of this object in a non-trivial way (e.g.,
     * incrementing a counter to ensure independent sessions), care must be taken that forks of this SUL manipulate the
     * same internal state.
     *
     * @return a fork of this SUL.
     *
     * @throws UnsupportedOperationException
     *         if this SUL can't be forked.
     */
    default SUL<I, O> fork() {
        throw new UnsupportedOperationException();
    }

}
