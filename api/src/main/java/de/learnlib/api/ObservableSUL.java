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
package de.learnlib.api;

import javax.annotation.Nonnull;

/**
 * A System Under Learning (SUL) where at any point in time the internal state can be observed.
 *
 * The main purpose of this interface is to check whether infinite words are accepted by the SUL.
 *
 * @param <S> the state type
 * @param <I> the input type
 * @param <O> the output type
 *
 * @author Jeroen Meijer
 */
public interface ObservableSUL<S, I, O> extends SUL<I, O> {

    @Nonnull
    @Override
    default ObservableSUL<S, I, O> fork() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the current state of the system.
     *
     * Implementation note: it is important that the returned Object has a well-defined {@link Object#equals(Object)}
     * method, and a good {@link Object#hashCode()} function.
     *
     * @return the current state of the system.
     */
    @Nonnull
    S getState();

    /**
     * Returns whether each state retrieved with {@link #getState()} is a deep copy.
     *
     * A state is a deep copy if calls to either {@link #step(Object)}, {@link #pre()}, or {@link #post()} do not modify
     * any state previously obtained with {@link #getState()}.
     *
     * More formally (assuming a perfect hash function): the result must be false if there is a case where in the
     * following statements the assertion does not hold:
     * {@code Object o = getState(); int hc = o.hashCode(); [step(...)|pre()|post()]; assert o.hashCode() == hc;}
     *
     * Furthermore, if states can be retrieved, but each state is not a deep copy, then this SUL <b>must</b> be
     * forkable, i.e. if !{@link #deepCopies()} then {@link #canFork()} must hold.
     *
     * @return whether each state is a deep copy.
     */
    default boolean deepCopies() {
        return false;
    }
}
