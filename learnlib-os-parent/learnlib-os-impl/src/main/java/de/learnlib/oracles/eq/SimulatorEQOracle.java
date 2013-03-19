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
package de.learnlib.oracles.eq;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.Query;



public class SimulatorEQOracle<A extends UniversalDeterministicAutomaton<?,I,?,?,?>,I,O>
	implements EquivalenceOracle<A, I, O> {
	
	private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
	
	public SimulatorEQOracle(UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference) {
		this.reference = reference;
	}

	@Override
	public Query<I, O> findCounterExample(A hypothesis, Alphabet<I> alphabet) {
		Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, alphabet);
		if(sep == null)
			return null;
		return new Query<>(sep); // FIXME: Output missing!
	}
	
}