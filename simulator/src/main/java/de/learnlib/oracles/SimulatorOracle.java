/* Copyright (C) 2014 TU Dortmund
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

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

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
public class SimulatorOracle<I, D> extends AbstractSingleQueryOracle<I, D> {
	
	
	public static class DFASimulatorOracle<I> extends SimulatorOracle<I,Boolean>
			implements DFAMembershipOracle<I> {
		public DFASimulatorOracle(DFA<?,I> dfa) {
			super(dfa);
		}
	}
	
	public static class MealySimulatorOracle<I,O> extends SimulatorOracle<I,Word<O>>
			implements MealyMembershipOracle<I,O> {
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
