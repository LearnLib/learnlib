/* Copyright (C) 2014 TU Dortmund
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
