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

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

/**
 * Implements an equivalence check by complete exploration up to a given depth, i.e.,
 * by testing all possible sequences of a certain length within a specified range.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
public class CompleteExplorationEQOracle<I, D> implements
		EquivalenceOracle<DetOutputAutomaton<?, I, ?, D>, I, D> {
	
	private int minDepth;
	private int maxDepth;
	private final MembershipOracle<I, D> sulOracle;
	
	/**
	 * Constructor.
	 * @param sulOracle interface to the system under learning
	 * @param maxDepth maximum exploration depth
	 */
	public CompleteExplorationEQOracle(MembershipOracle<I, D> sulOracle, int maxDepth) {
		this(sulOracle, 1, maxDepth);
	}
	
	/**
	 * Constructor.
	 * @param sulOracle interface to the system under learning
	 * @param minDepth minimum exploration depth
	 * @param maxDepth maximum exploration depth
	 */
	public CompleteExplorationEQOracle(MembershipOracle<I, D> sulOracle, int minDepth, int maxDepth) {
		if(maxDepth < minDepth)
			maxDepth = minDepth;
		
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
		
		this.sulOracle = sulOracle;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, D> findCounterExample(DetOutputAutomaton<?,I,?,D> hypothesis,
			Collection<? extends I> alphabet) {
		for(List<? extends I> symList : CollectionsUtil.allTuples(alphabet, minDepth, maxDepth)) {
			Word<I> queryWord = Word.fromList(symList);
			
			DefaultQuery<I,D> query = new DefaultQuery<>(queryWord);
			D hypOutput = hypothesis.computeOutput(queryWord);
			sulOracle.processQueries(Collections.singleton(query));
			
			if(!Objects.equals(hypOutput, query.getOutput()))
				return query;
		}
		
		return null;
	}

}
