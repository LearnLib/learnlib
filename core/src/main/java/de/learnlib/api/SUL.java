/* Copyright (C) 2013-2015 TU Dortmund
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
import javax.annotation.Nullable;

/**
 * Interface for a system under learning (SUL) that can make single steps.
 *
 * @param <I> input symbols
 * @param <O> output symbols
 *
 * @author falkhowar
 * @author Malte Isberner
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
     * @param in input to the SUL
     * @return output of SUL
     */
    @Nullable
    O step(@Nullable I in) throws SULException;
    
    
    /**
     * Returns whether this SUL is capable of {@link #fork() forking}.
     * @return {@code true} if this SUL can be forked, {@code false} otherwise
     * @see #fork()
     */
    default public boolean canFork() {
    	return false;
    }
    
    /**
     * Forks this SUL, if possible. The fork of a SUL is a copy which behaves exactly
     * the same as this SUL. This method should always return a reseted SUL, regardless
     * of whether this call is made between a call to {@link #pre()} and {@link #post()}.
     * <p>
     * If {@link #canFork()} returns {@code true}, this method must return a non-{@code null}
     * object, which should behave exactly like this SUL (in particular, it must be forkable
     * as well). Otherwise, a {@link UnsupportedOperationException} must be thrown.
     * <p>
     * Implementation note: if resetting a SUL changes the internal state of this object in a
     * non-trivial way (e.g., incrementing a counter to ensure independent sessions), care must
     * be taken that forks of this SUL manipulate the same internal state.
     * @return a fork of this SUL.
     * @throws UnsupportedOperationException if this SUL can't be forked.
     */
    @Nonnull
    default public SUL<I,O> fork() throws UnsupportedOperationException {
    	throw new UnsupportedOperationException();
    }
}
