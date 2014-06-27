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
import de.learnlib.mapper.api.ContextExecutableInput;

/**
 * A {@link SUL} that executes {@link ContextExecutableInput} symbols.
 * <p>
 * The creation and disposal of contexts is delegated to an external {@link ContextHandler}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 * @param <C> context type
 */
public class ContextExecutableInputSUL<I extends ContextExecutableInput<? extends O, ? super C>, O, C>
		extends AbstractContextExecutableInputSUL<I, O, C> {
	
	public static interface ContextHandler<C> {
		public C createContext();
		public void disposeContext(C context);
	}
	
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
	
}
