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
package de.learnlib.api.exception;

import javax.annotation.Nonnull;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle;

/**
 * Unchecked exception class that can be used by implementors of a {@link SUL} to wrap any exceptions that occur during
 * the {@link SUL#step(Object)} methods.
 * <p>
 * <b>Rationale for being unchecked:</b> Implementors of a learning or equivalence checking algorithm that directly
 * operates on the SUL level usually have no sensible way of dealing with such an exception (comparable to when {@link
 * MembershipOracle#processQueries(java.util.Collection)} throws a runtime exception). However, it may be of interest to
 * some components, like for instance a mapper that maps exceptions to special output symbols.
 * <p>
 * <b>Caveat:</b> When implementing your {@link SUL#step(Object)} method, <b>never ever</b> catch exceptions with a
 * {@code catch(Throwable)} statement! This would also catch internal VM-related errors such as {@link
 * StackOverflowError} or {@link OutOfMemoryError}. Only ever catch {@link Exception} and any user-created subclasses of
 * {@link Throwable} that apply.
 *
 * @author Malte Isberner
 */
public class SULException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a SULException wrapped around a {@link Throwable}. Please refer to the class description on how this
     * mechanism is to be used.
     *
     * @param cause
     *         the exception cause, should <b>never</b> be a subclass of {@link Error}.
     */
    public SULException(@Nonnull Throwable cause) {
        super(cause);
    }

}
