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

	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,O>,I,O>
	WMethodEQOracle<A, I, O> wMethod(int maxDepth, MembershipOracle<I, O> sulOracle) {
		return new WMethodEQOracle<>(maxDepth, sulOracle);
	}
	
	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,O>,I,O>
	WpMethodEQOracle<A, I, O> wpMethod(int maxDepth, MembershipOracle<I,O> sulOracle) {
		return new WpMethodEQOracle<>(maxDepth, sulOracle);
	}
	
	public static <A extends UniversalDeterministicAutomaton<?,I,?,?,?> & Output<I,O>,I,O>
	SimulatorEQOracle<I, O> simulator(A target) {
		A automaton = target;
		return new SimulatorEQOracle<I,O>(automaton);
	}

}
