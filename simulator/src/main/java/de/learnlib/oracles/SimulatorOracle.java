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
package de.learnlib.oracles;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.api.SingleQueryOracle;

/**
 * A membership oracle backed by an automaton. The automaton must implement
 * the {@link SuffixOutput} concept, allowing to identify a suffix part in the output
 * (relative to a prefix/suffix subdivision in the input).
 *   
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> (suffix) output domain type
 */
public class SimulatorOracle<I, D> implements SingleQueryOracle<I, D> {

	public static class DFASimulatorOracle<I> extends SimulatorOracle<I,Boolean>
			implements SingleQueryOracleDFA<I> {
		public DFASimulatorOracle(DFA<?,I> dfa) {
			super(dfa);
		}
	}
	
	public static class MealySimulatorOracle<I,O> extends SimulatorOracle<I,Word<O>>
			implements SingleQueryOracleMealy<I,O> {
		public MealySimulatorOracle(MealyMachine<?,I,?,O> mealy) {
			super(mealy);
		}
	}
	
	
	private final SuffixOutput<I, D> automaton;
	
	/**
	 * Constructor.
	 * @param automaton the suffix-observable automaton
	 */
	public SimulatorOracle(SuffixOutput<I,D> automaton) {
		this.automaton = automaton;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public D answerQuery(Word<I> prefix, Word<I> suffix) {
		return automaton.computeSuffixOutput(prefix, suffix);
	}
	
}
