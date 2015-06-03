/* Copyright (C) 2013-2015 TU Dortmund
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
package de.learnlib.examples.dfa;

import net.automatalib.automata.fsa.MutableDFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.examples.DefaultLearningExample.DefaultDFALearningExample;

/**
 * This class provides the example used in the paper ''Learning Regular Sets
 * from Queries and Counterexamples'' by Dana Angluin that consists of an
 * automaton that accepts ''all strings over {0,1} with an even number of 0's
 * and an even number of 1's.''
 * 
 * @author Oliver Bauer 
 */
public class ExampleAngluin extends DefaultDFALearningExample<Integer> {


	public static Alphabet<Integer> createInputAlphabet() {
		return Alphabets.integers(0, 1);
	}
	
	
	
	public static <A extends MutableDFA<S, ? super Integer>,S>
	A constructMachine(A machine) {
		
//		S q0 = machine.addInitialState(true);
//		S q1 = machine.addState(false), q2 = machine.addState(false), q3 = machine
//				.addState(false);
//
//		machine.addTransition(q0, 0, q1);
//		machine.addTransition(q0, 1, q2);
//
//		machine.addTransition(q1, 0, q0);
//		machine.addTransition(q1, 1, q3);
//
//		machine.addTransition(q2, 0, q3);
//		machine.addTransition(q2, 1, q0);
//
//		machine.addTransition(q3, 0, q2);
//		machine.addTransition(q3, 1, q1);

	machine = AutomatonBuilders.forDFA(machine)
				.from("q0")
					.on(0).to("q1")
					.on(1).to("q2")
				.from("q1")
					.on(0).to("q0")
					.on(1).to("q3")
				.from("q2")
					.on(0).to("q3")
					.on(1).to("q0")
				.from("q3")
					.on(0).to("q2")
					.on(1).to("q3")
				.withAccepting("q0")
				.withInitial("q0")
			.create();
		
		return machine;
	}
	
	public static CompactDFA<Integer> constructMachine() {
		return constructMachine(new CompactDFA<>(createInputAlphabet()));
	}

	
	public static ExampleAngluin createExample() {
		return new ExampleAngluin();
	}

	
	
	public ExampleAngluin() {
		super(constructMachine());
	}
}
