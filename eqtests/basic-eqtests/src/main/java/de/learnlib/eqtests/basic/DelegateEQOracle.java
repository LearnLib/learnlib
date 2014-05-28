/* Copyright (C) 2013 TU Dortmund
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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.eqtests.basic;

import java.util.Collection;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public abstract class DelegateEQOracle<A, I, D> implements
		EquivalenceOracle<A, I, D> {
	
	protected final EquivalenceOracle<? super A, I, D> delegate;

	public DelegateEQOracle(EquivalenceOracle<? super A, I, D> delegate) {
		this.delegate = delegate;
	}

	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		return delegate.findCounterExample(hypothesis, inputs);
	}

}
