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
package de.learnlib.algorithms.lstargeneric;

import java.util.Collections;
import java.util.List;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public abstract class ExtensibleAutomatonLStar<A,I,O,S,T,SP,TP,AI extends MutableDeterministic<S,I,T,SP,TP>> extends
AbstractAutomatonLStar<A, I, O,S,T,SP,TP,AI> {
	
	public static final class BuilderDefaults {
		public static final <I> List<Word<I>> initialSuffixes() {
			return Collections.emptyList();
		}
		
		public static final <I,O> ObservationTableCEXHandler<? super I, ? super O> cexHandler() {
			return ObservationTableCEXHandlers.CLASSIC_LSTAR;
		}
		
		public static final <I,O> ClosingStrategy<? super I, ? super O> closingStrategy() {
			return ClosingStrategies.CLOSE_FIRST;
		}
		
	}
	
	protected final ObservationTableCEXHandler<? super I, ? super O> cexHandler;
	protected final ClosingStrategy<? super I, ? super O> closingStrategy;
	protected final List<Word<I>> initialSuffixes;
	
	public ExtensibleAutomatonLStar(Alphabet<I> alphabet,
			MembershipOracle<I,O> oracle, AI internalHyp,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I,? super O> cexHandler,
			ClosingStrategy<? super I,? super O> closingStrategy) {
		super(alphabet, oracle, internalHyp);
		this.initialSuffixes = initialSuffixes;
		this.cexHandler = cexHandler;
		this.closingStrategy = closingStrategy;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.lstargeneric.AbstractLStar#doRefineHypothesis(de.learnlib.oracles.DefaultQuery)
	 */
	@Override
	protected void refineHypothesisInternal(DefaultQuery<I, O> ceQuery) {
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
	
	protected abstract SuffixOutput<I,O> hypothesisOutput();
	
	
}
