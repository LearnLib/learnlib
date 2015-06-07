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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterables;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.MQUtil;

/**
 * Implements an equivalence test by applying the Wp-method test on the given hypothesis automaton,
 * as described in "Test Selection Based on Finite State Models" by S. Fujiwara et al.
 * 
 * @author Malte Isberner
 *
 * @param <A> automaton type
 * @param <I> input symbol type
 * @param <D> output domain type
 */
public class WpMethodEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I,D>,I,D>
		implements EquivalenceOracle<A, I, D> {
	
	public static class DFAWpMethodEQOracle<I> extends WpMethodEQOracle<DFA<?,I>,I,Boolean>
			implements DFAEquivalenceOracle<I> {
		public DFAWpMethodEQOracle(int maxDepth,
				MembershipOracle<I, Boolean> sulOracle) {
			super(maxDepth, sulOracle);
		}
	}
	
	public static class MealyWpMethodEQOracle<I,O> extends WpMethodEQOracle<MealyMachine<?,I,?,O>,I,Word<O>> {
		public MealyWpMethodEQOracle(int maxDepth,
				MembershipOracle<I, Word<O>> sulOracle) {
			super(maxDepth, sulOracle);
		}
	}
	
	private final int maxDepth;
	private final MembershipOracle<I, D> sulOracle;
	
	/**
	 * Constructor.
	 * @param maxDepth the maximum length of the "middle" part of the test cases
	 * @param sulOracle interface to the system under learning
	 */
	public WpMethodEQOracle(int maxDepth, MembershipOracle<I,D> sulOracle) {
		this.maxDepth = maxDepth;
		this.sulOracle = sulOracle;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis,
			Collection<? extends I> inputs) {
		UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
		Output<I,D> out = hypothesis;
		return doFindCounterExample(aut, out, inputs);
	}
	
	
	/*
	 * Delegate target, used to bind the state-parameter of the automaton
	 */
	private <S> DefaultQuery<I,D> doFindCounterExample(UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
			Output<I,D> output, Collection<? extends I> inputs) {
		
		List<Word<I>> stateCover = new ArrayList<Word<I>>(hypothesis.size());
		List<Word<I>> transitions = new ArrayList<Word<I>>(hypothesis.size() * (inputs.size() - 1));
		
		Automata.cover(hypothesis, inputs, stateCover, transitions);
		
		List<Word<I>> globalSuffixes = Automata.characterizingSet(hypothesis, inputs);
		if(globalSuffixes.isEmpty())
			globalSuffixes = Collections.singletonList(Word.<I>epsilon());
	
		WordBuilder<I> wb = new WordBuilder<>();
		
		
		// Phase 1: state cover * middle part * global suffixes
		for(List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
			for(Word<I> as : stateCover) {
				for(Word<I> suffix : globalSuffixes) {
					wb.append(as).append(middle).append(suffix);
					Word<I> queryWord = wb.toWord();
					wb.clear();
					DefaultQuery<I,D> query = new DefaultQuery<>(queryWord);
					D hypOutput = output.computeOutput(queryWord);
					sulOracle.processQueries(Collections.singleton(query));
					if(!Objects.equals(hypOutput, query.getOutput()))
						return query;
				}
			}
		}

		// Phase 2: transitions (not in state cover) * middle part * local suffixes
		MutableMapping<S,List<Word<I>>> localSuffixSets
			= hypothesis.createStaticStateMapping();
		
		for(List<? extends I> middle : CollectionsUtil.allTuples(inputs, 1, maxDepth)) {
			for(Word<I> trans : transitions) {
				S state = hypothesis.getState(Iterables.concat(trans, middle));
				List<Word<I>> localSuffixes = localSuffixSets.get(state);
				if(localSuffixes == null) {
					localSuffixes = Automata.stateCharacterizingSet(hypothesis, inputs, state);
					if(localSuffixes.isEmpty()) {
						localSuffixes = Collections.singletonList(Word.<I>epsilon());
					}
					localSuffixSets.put(state, localSuffixes);
				}
				
				for(Word<I> suffix : localSuffixes) {
					wb.append(trans).append(middle).append(suffix);
					Word<I> queryWord = wb.toWord();
					wb.clear();
					DefaultQuery<I,D> query = MQUtil.query(sulOracle, queryWord);
					D hypOutput = output.computeOutput(queryWord);
					if(!Objects.equals(hypOutput, query.getOutput())) {
						return query;
					}
				}
			}
		}
		
		return null;
	}

}
