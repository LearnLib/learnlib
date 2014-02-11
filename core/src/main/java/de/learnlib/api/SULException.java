/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.api;

import javax.annotation.Nonnull;

/**
 * Unchecked exception class that can be used by implementors
 * of a {@link SUL} to wrap any exceptions that occur during the
 * {@link SUL#step(Object)} methods.
 * <p>
 * <b>Rationale for being unchecked:</b> Implementors of a learning or equivalence checking
 * algorithm that directly operates on the SUL level usually have no sensible way of
 * dealing with such an exception (comparable to when
 * {@link MembershipOracle#processQueries(java.util.Collection)} throws a runtime exception).
 * However, it may be of interest to some components, like for instance a mapper that
 * maps exceptions to special output symbols. 
 * <p>
 * <b>Caveat:</b> When implementing your {@link SUL#step(Object)} method,
 * <b>never ever</b> catch exceptions with a {@code catch(Throwable)} statement!
 * This would also catch internal VM-related errors such as {@link StackOverflowError}
 * or {@link OutOfMemoryError}. Only ever catch {@link Exception} and any user-created
 * subclasses of {@link Throwable} that apply.
 * 
 * @author Malte Isberner
 *
 */
public class SULException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a SULException wrapped around a {@link Throwable}. Please refer to the
	 * class description on how this mechanism is to be used.
	 * 
	 * @param cause the exception cause, should <b>never</b> be a subclass of {@link Error}.
	 */
	public SULException(@Nonnull Throwable cause) {
		super(cause);
	}
	
}
