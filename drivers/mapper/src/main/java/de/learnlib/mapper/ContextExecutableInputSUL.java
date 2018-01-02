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
package de.learnlib.mapper;

import de.learnlib.api.SUL;
import de.learnlib.mapper.api.ContextExecutableInput;

/**
 * A {@link SUL} that executes {@link ContextExecutableInput} symbols.
 * <p>
 * The creation and disposal of contexts is delegated to an external {@link ContextHandler}.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <C>
 *         context type
 *
 * @author Malte Isberner
 */
public class ContextExecutableInputSUL<I extends ContextExecutableInput<? extends O, ? super C>, O, C>
        extends AbstractContextExecutableInputSUL<I, O, C> {

    private final ContextHandler<C> contextHandler;

    public ContextExecutableInputSUL(ContextHandler<C> contextHandler) {
        this.contextHandler = contextHandler;
    }

    @Override
    protected C createContext() {
        return contextHandler.createContext();
    }

    @Override
    protected void disposeContext(C context) {
        contextHandler.disposeContext(context);
    }

    @Override
    public boolean canFork() {
        return true;
    }

    @Override
    public SUL<I, O> fork() {
        return new ContextExecutableInputSUL<>(contextHandler);
    }

    /**
     * Facility for creating and disposing of contexts on which {@link ContextExecutableInput}s operate.
     * <p>
     * An implementation of this interface must be thread-safe, i.e., both the {@link #createContext()} and {@link
     * #disposeContext(Object)} methods must be reentrant. Furthermore, it must not make any assumptions as to the
     * particular sequence in which these methods are called.
     *
     * @param <C>
     *         context type
     *
     * @author Malte Isberner
     */
    public interface ContextHandler<C> {

        C createContext();

        void disposeContext(C context);
    }
}
