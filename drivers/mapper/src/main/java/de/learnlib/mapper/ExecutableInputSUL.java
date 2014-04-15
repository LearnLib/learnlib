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
import de.learnlib.mapper.api.ExecutableInput;

/**
 * A {@link SUL} that executes {@link ExecutableInput} symbols.
 * 
 * @author Malte Isberner
 * 
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class ExecutableInputSUL<I extends ExecutableInput<? extends O>, O> implements SUL<I, O> {

	@Override
	public void pre() {
	}

	@Override
	public void post() {
	}

	@Override
	public O step(I in) throws SULException {
		try {
			return in.execute();
		}
		catch(SULException ex) {
			throw ex;
		}
		catch(Exception ex) {
			throw new SULException(ex);
		}
	}
}
