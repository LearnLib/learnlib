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
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.eqtests.basic;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class SimpleEQOracle<A extends InputAlphabetHolder<I>,I,D> {
	
	public static <A extends InputAlphabetHolder<I>,I,D>
	SimpleEQOracle<A,I,D> create(EquivalenceOracle<A,I,D> eqOracle) {
		return new SimpleEQOracle<A,I,D>(eqOracle);
	}
	
	private final EquivalenceOracle<A, I, D> eqOracle;
	
	public SimpleEQOracle(EquivalenceOracle<A,I,D> eqOracle) {
		this.eqOracle = eqOracle;
	}
	
	public DefaultQuery<I,D> findCounterExample(A hypothesis) {
		return eqOracle.findCounterExample(hypothesis, hypothesis.getInputAlphabet());
	}
}
