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
package de.learnlib.driver;

import de.learnlib.sul.ContextExecutableInput;
import de.learnlib.sul.ContextHandler;
import de.learnlib.sul.SUL;

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

}
