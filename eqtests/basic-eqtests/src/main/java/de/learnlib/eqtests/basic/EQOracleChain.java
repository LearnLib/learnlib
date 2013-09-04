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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class EQOracleChain<A, I, O> implements EquivalenceOracle<A, I, O> {
	
	private final List<EquivalenceOracle<? super A, I, O>> oracles;

	public EQOracleChain(List<? extends EquivalenceOracle<? super A,I,O>> oracles) {
		this.oracles = new ArrayList<>(oracles);
	}
	
	@SafeVarargs
	public EQOracleChain(EquivalenceOracle<? super A,I,O> ...oracles) {
		this(Arrays.asList(oracles));
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		for(EquivalenceOracle<? super A,I,O> eqOracle : oracles) {
			DefaultQuery<I,O> ceQry = eqOracle.findCounterExample(hypothesis, inputs);
			if(ceQry != null)
				return ceQry;
		}
		return null;
	}

}
