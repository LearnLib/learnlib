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
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.concepts.DetOutputAutomaton;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Word;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class CompleteExplorationEQOracle<I, O> implements
		EquivalenceOracle<DetOutputAutomaton<?, I, ?, O>, I, O> {
	
	private int minDepth;
	private int maxDepth;
	private final MembershipOracle<I, O> sulOracle;
	
	public CompleteExplorationEQOracle(MembershipOracle<I, O> sulOracle, int maxDepth) {
		this(sulOracle, 1, maxDepth);
	}
	
	public CompleteExplorationEQOracle(MembershipOracle<I, O> sulOracle, int minDepth, int maxDepth) {
		if(maxDepth < minDepth)
			maxDepth = minDepth;
		
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
		
		this.sulOracle = sulOracle;
	}

	@Override
	public DefaultQuery<I, O> findCounterExample(DetOutputAutomaton<?,I,?,O> hypothesis,
			Collection<? extends I> alphabet) {
		for(List<? extends I> symList : CollectionsUtil.allTuples(alphabet, minDepth, maxDepth)) {
			Word<I> queryWord = Word.fromList(symList);
			
			DefaultQuery<I,O> query = new DefaultQuery<>(queryWord);
			O hypOutput = hypothesis.computeOutput(queryWord);
			sulOracle.processQueries(Collections.singleton(query));
			
			if(!CmpUtil.equals(hypOutput, query.getOutput()))
				return query;
		}
		
		return null;
	}

}
