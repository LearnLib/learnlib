/* Copyright (C) 2013 TU Dortmund
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
