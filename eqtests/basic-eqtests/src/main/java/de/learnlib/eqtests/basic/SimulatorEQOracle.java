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

import java.util.Collection;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;



public class SimulatorEQOracle<I,D>
	implements EquivalenceOracle<UniversalDeterministicAutomaton<?, I, ?, ?, ?>, I, D> {
	
	public static class DFASimulatorEQOracle<I> implements DFAEquivalenceOracle<I> {
		private final SimulatorEQOracle<I, Boolean> delegate;
		public DFASimulatorEQOracle(DFA<?,I> dfa) {
			this.delegate = new SimulatorEQOracle<>(dfa);
		}
		@Override
		public DefaultQuery<I, Boolean> findCounterExample(
				DFA<?, I> hypothesis, Collection<? extends I> inputs) {
			return delegate.findCounterExample(hypothesis, inputs);
		}
	}
	
	public static class MealySimulatorEQOracle<I,O> implements MealyEquivalenceOracle<I, O> {
		private final SimulatorEQOracle<I, Word<O>> delegate;
		public MealySimulatorEQOracle(MealyMachine<?, I, ?, O> mealy) {
			this.delegate = new SimulatorEQOracle<>(mealy);
		}
		@Override
		public DefaultQuery<I, Word<O>> findCounterExample(
				MealyMachine<?, I, ?, O> hypothesis,
				Collection<? extends I> inputs) {
			return delegate.findCounterExample(hypothesis, inputs);
		}
		
	}
	
	private final UniversalDeterministicAutomaton<?, I, ?, ?, ?> reference;
	private final Output<I, D> output;
	
	public <S,T,R extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>>
			SimulatorEQOracle(R reference) {
		this.reference = reference;
		this.output = reference;
	}

	@Override
	public DefaultQuery<I, D> findCounterExample(UniversalDeterministicAutomaton<?, I, ?, ?, ?> hypothesis, Collection<? extends I> inputs) {
		Word<I> sep = Automata.findSeparatingWord(reference, hypothesis, inputs);
		if(sep == null) {
			return null;
		}
		D out = output.computeOutput(sep);
		DefaultQuery<I,D> qry = new DefaultQuery<>(sep);
		qry.answer(out);
		return qry;
	}
	
}