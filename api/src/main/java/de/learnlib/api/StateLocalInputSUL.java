/* Copyright (C) 2013-2019 TU Dortmund
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

import java.util.Collection;

import de.learnlib.api.exception.SULException;

/**
 * A System Under Learning (SUL) which can additionally report the inputs that the SUL can process in its current state,
 * i.e. inputs that will not trigger a {@link SULException} when used in the next invocation of the {@link
 * #step(Object)} method return an otherwise "undefined" behavior.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Maren Geske
 * @author frohme
 */
public interface StateLocalInputSUL<I, O> extends SUL<I, O> {

    Collection<I> currentlyEnabledInputs();

    @Override
    default StateLocalInputSUL<I, O> fork() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
