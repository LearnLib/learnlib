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

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import de.learnlib.api.MembershipOracle;

public abstract class EquivalenceQueries {

	private EquivalenceQueries() {
	}

	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,D>,I,D>
	WMethodEQOracle<A, I, D> wMethod(int maxDepth, MembershipOracle<I, D> sulOracle) {
		return new WMethodEQOracle<A,I,D>(maxDepth, sulOracle);
	}
	
	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,D>,I,D>
	WpMethodEQOracle<A, I, D> wpMethod(int maxDepth, MembershipOracle<I,D> sulOracle) {
		return new WpMethodEQOracle<A,I,D>(maxDepth, sulOracle);
	}
	
	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,D>,I,D>
	SimulatorEQOracle<I, D> simulator(A target) {
		A automaton = target;
		return new SimulatorEQOracle<I,D>(automaton);
	}

}
