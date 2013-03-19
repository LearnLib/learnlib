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
package de.learnlib.oracles;

import java.util.Collection;

import net.automatalib.automata.concepts.SODetOutputAutomaton;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

public class SimulatorOracle<I, O> implements MembershipOracle<I, O> {
	
	private final SODetOutputAutomaton<?, I, ?, O> automaton;
	
	public SimulatorOracle(SODetOutputAutomaton<?,I,?,O> automaton) {
		this.automaton = automaton;
	}
	
	@Override
	public void processQueries(Collection<Query<I, O>> queries) {
		for(Query<I,O> q : queries) {
			O output = automaton.computeSuffixOutput(q.getPrefix(), q.getSuffix());
			q.setOutput(output);
		}
	}
	
}
