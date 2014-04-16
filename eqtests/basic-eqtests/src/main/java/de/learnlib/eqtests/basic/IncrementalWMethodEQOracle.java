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
package de.learnlib.eqtests.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.util.automata.conformance.IncrementalWMethodTestsIterator;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class IncrementalWMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?,?> & Output<I,O>, I, O>
		implements EquivalenceOracle<A, I, O> {
	
	public static class DFAIncrementalWMethodEQOracle<I>
			extends IncrementalWMethodEQOracle<DFA<?,I>, I, Boolean>
			implements DFAEquivalenceOracle<I> {
		public DFAIncrementalWMethodEQOracle(Alphabet<I> alphabet,
				MembershipOracle<I, Boolean> oracle, int maxDepth) {
			super(alphabet, oracle, maxDepth);
		}
		public DFAIncrementalWMethodEQOracle(Alphabet<I> alphabet,
				MembershipOracle<I, Boolean> oracle) {
			super(alphabet, oracle);
		}
	}
	
	public static class MealyIncrementalWMethodEQOracle<I,O>
			extends IncrementalWMethodEQOracle<MealyMachine<?,I,?,O>, I, Word<O>>
			implements MealyEquivalenceOracle<I, O> {
		public MealyIncrementalWMethodEQOracle(Alphabet<I> alphabet,
				MembershipOracle<I, Word<O>> oracle, int maxDepth) {
			super(alphabet, oracle, maxDepth);
		}
		public MealyIncrementalWMethodEQOracle(Alphabet<I> alphabet,
				MembershipOracle<I, Word<O>> oracle) {
			super(alphabet, oracle);
		}
	}
	
	@SuppressWarnings("unused")
	private final Alphabet<I> alphabet;
	private final MembershipOracle<I, O> oracle;
	private final IncrementalWMethodTestsIterator<I> incrementalWMethodIt;
	
	private int maxDepth;
	
	public IncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, O> oracle) {
		this(alphabet, oracle, 1);
	}
	
	public IncrementalWMethodEQOracle(Alphabet<I> alphabet, MembershipOracle<I, O> oracle, int maxDepth) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		this.incrementalWMethodIt = new IncrementalWMethodTestsIterator<>(alphabet);
		this.incrementalWMethodIt.setMaxDepth(maxDepth);
		
		this.maxDepth = maxDepth;
	}

	public int getMaxDepth() {
		return maxDepth;
	}
	
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	@Override
	public DefaultQuery<I, O> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		// FIXME: warn about inputs being ignored?
		incrementalWMethodIt.update(hypothesis);
		
		while(incrementalWMethodIt.hasNext()) {
			Word<I> testCase = incrementalWMethodIt.next();
			
			DefaultQuery<I, O> query = new DefaultQuery<>(testCase);
			oracle.processQueries(Collections.singleton(query));
			O hypOut = hypothesis.computeOutput(testCase);
			if(!Objects.equals(query.getOutput(), hypOut)) {
				// found counterexample
				return query;
			}
		}
		
		return null;
	}
	
	

}
