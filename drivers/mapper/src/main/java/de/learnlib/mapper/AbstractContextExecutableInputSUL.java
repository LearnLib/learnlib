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
package de.learnlib.mapper;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.learnlib.mapper.api.ContextExecutableInput;

/**
 * Abstract base class for a {@link SUL} that step-wisely executes {@link ContextExecutableInput}
 * symbols.
 * <p>
 * This class does not specify how contexts are created and disposed of, but declares abstract
 * methods for these tasks.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 * @param <C> context type
 */
public abstract class AbstractContextExecutableInputSUL<I extends ContextExecutableInput<? extends O,? super C>, O, C> implements SUL<I,O> {
	
	protected abstract C createContext();
	protected abstract void disposeContext(C context);
	
	private C currentContext;
	
	@Override
	public void pre() {
		this.currentContext = createContext();
	}
	
	@Override
	public void post() {
		disposeContext(currentContext);
		currentContext = null;
	}
	
	@Override
	public O step(I in) throws SULException {
		try {
			return in.execute(currentContext);
		}
		catch(SULException ex) {
			throw ex;
		}
		catch(Exception ex) {
			throw new SULException(ex);
		}
	}
}
