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
package de.learnlib.eqtests.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

/**
 * Implements an equivalence test by applying the W-method test on the given
 * hypothesis automaton, as described in "Testing software design modeled by finite state machines"
 * by T.S. Chow.
 * 
 * @author Malte Isberner
 *
 * @param <A> automaton type
 * @param <I> input symbol type
 * @param <D> output domain type
 */
public class WMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?,?> & Output<I,D>, I, D>
	implements EquivalenceOracle<A, I, D> {
	
	public static class DFAWMethodEQOracle<I> extends WMethodEQOracle<DFA<?,I>,I,Boolean>
			implements DFAEquivalenceOracle<I> {
		public DFAWMethodEQOracle(int maxDepth,
				MembershipOracle<I, Boolean> sulOracle) {
			super(maxDepth, sulOracle);
		}
	}
	
	public static class MealyWMethodEQOracle<I,O> extends WMethodEQOracle<MealyMachine<?,I,?,O>,I,Word<O>>
			implements MealyEquivalenceOracle<I,O> {
		public MealyWMethodEQOracle(int maxDepth,
				MembershipOracle<I, Word<O>> sulOracle) {
			super(maxDepth, sulOracle);
		}
	}
	
	private int maxDepth;
	private final MembershipOracle<I,D> sulOracle;
	
	/**
	 * Constructor.
	 * @param maxDepth the maximum length of the "middle" part of the test cases
	 * @param sulOracle interface to the system under learning
	 */
	public WMethodEQOracle(int maxDepth, MembershipOracle<I,D> sulOracle) {
		this.maxDepth = maxDepth;
		this.sulOracle = sulOracle;
	}
	
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		
		List<Word<I>> transCover = Automata.transitionCover(hypothesis, inputs);
		List<Word<I>> charSuffixes = Automata.characterizingSet(hypothesis, inputs);
		
		// Special case: List of characterizing suffixes may be empty,
		// but in this case we still need to test!
		if(charSuffixes.isEmpty())
			charSuffixes = Collections.singletonList(Word.<I>epsilon());
		
		
		WordBuilder<I> wb = new WordBuilder<>();
		
		for(List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
			for(Word<I> trans : transCover) {
				for(Word<I> suffix : charSuffixes) {
					wb.append(trans).append(middle).append(suffix);
					Word<I> queryWord = wb.toWord();
					wb.clear();
					DefaultQuery<I,D> query = new DefaultQuery<>(queryWord);
					D hypOutput = hypothesis.computeOutput(queryWord);
					sulOracle.processQueries(Collections.singleton(query));
					if(!Objects.equals(hypOutput, query.getOutput()))
						return query;
				}
			}
		}
		
		return null;
	}
	
}