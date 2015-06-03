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
package de.learnlib.algorithms.lstargeneric;

import java.util.Collections;
import java.util.List;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public abstract class ExtensibleAutomatonLStar<A,I,D,S,T,SP,TP,AI extends MutableDeterministic<S,I,T,SP,TP>> extends
AbstractAutomatonLStar<A, I, D,S,T,SP,TP,AI> {
	
	public static final class BuilderDefaults {
		public static final <I> List<Word<I>> initialPrefixes() {
			return Collections.singletonList(Word.<I>epsilon());
		}
		public static final <I> List<Word<I>> initialSuffixes() {

			return Collections.emptyList();
		}
		
		public static <I,D> ObservationTableCEXHandler<? super I, ? super D> cexHandler() {
			return ObservationTableCEXHandlers.CLASSIC_LSTAR;
		}
		
		public static <I,D> ClosingStrategy<? super I, ? super D> closingStrategy() {
			return ClosingStrategies.CLOSE_FIRST;
		}
		
	}
	
	protected final ObservationTableCEXHandler<? super I, ? super D> cexHandler;
	protected final ClosingStrategy<? super I, ? super D> closingStrategy;
	protected final List<Word<I>> initialPrefixes;
	protected final List<Word<I>> initialSuffixes;
	
	public ExtensibleAutomatonLStar(Alphabet<I> alphabet,
			MembershipOracle<I,D> oracle, AI internalHyp,
			List<Word<I>> initialPrefixes,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I,? super D> cexHandler,
			ClosingStrategy<? super I,? super D> closingStrategy) {
		super(alphabet, oracle, internalHyp);
		this.initialPrefixes = initialPrefixes;
		this.initialSuffixes = initialSuffixes;
		this.cexHandler = cexHandler;
		this.closingStrategy = closingStrategy;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.lstargeneric.AbstractLStar#doRefineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
	@Override
	protected void refineHypothesisInternal(DefaultQuery<I, D> ceQuery) {
		List<List<Row<I>>> unclosed = cexHandler.handleCounterexample(ceQuery, table, hypothesisOutput(), oracle);
		completeConsistentTable(unclosed, cexHandler.needsConsistencyCheck());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.lstargeneric.AbstractLStar#selectClosingRows(java.util.List)
	 */
	@Override
	protected List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosed) {
		return closingStrategy.selectClosingRows(unclosed, table, oracle);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.lstargeneric.AbstractLStar#initialSuffixes()
	 */
	@Override
	protected List<Word<I>> initialSuffixes() {
		return initialSuffixes;
	}
	
	@Override
	protected List<Word<I>> initialPrefixes() {
		return initialPrefixes;
	}
	
	
}
