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
package de.learnlib.algorithms.lstargeneric.mealy;


import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.automata.transout.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.features.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.algorithms.lstargeneric.ExtensibleAutomatonLStar;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class ExtensibleLStarMealy<I, O> extends
		ExtensibleAutomatonLStar<MealyMachine<?,I,?,O>, I, Word<O>, Integer, CompactMealyTransition<O>, Void, O, CompactMealy<I,O>>
		implements OTLearnerMealy<I,O> {
	
	private final List<O> outputTable
		= new ArrayList<O>();
	

	@GenerateBuilder(defaults = ExtensibleAutomatonLStar.BuilderDefaults.class)
	public ExtensibleLStarMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<? super I, ? super Word<O>> cexHandler,
			ClosingStrategy<? super I, ? super Word<O>> closingStrategy) {
		super(alphabet, oracle, new CompactMealy<I,O>(alphabet),
				LStarMealyUtil.ensureSuffixCompliancy(initialSuffixes, alphabet, cexHandler.needsConsistencyCheck()),
				cexHandler,
				closingStrategy);
	}

	@Override
	protected void updateInternalHypothesis() {
		updateOutputs();
		super.updateInternalHypothesis();
	}

	@Override
	protected Void stateProperty(Row<I> stateRow) {
		return null;
	}

	@Override
	protected O transitionProperty(Row<I> stateRow, int inputIdx) {
		Row<I> transRow = stateRow.getSuccessor(inputIdx);
		return outputTable.get(transRow.getRowId() - 1);
	}

	@Override
	protected List<Word<I>> initialSuffixes() {
		return initialSuffixes;
	}
	
	protected void updateOutputs() {
		int numOutputs = outputTable.size();
		int numTransRows = table.numTotalRows() - 1;
		
		int newOutputs = numTransRows - numOutputs;
		if(newOutputs == 0)
			return;
		
		List<DefaultQuery<I,Word<O>>> outputQueries
			= new ArrayList<DefaultQuery<I,Word<O>>>(numOutputs);
		
		for(int i = numOutputs+1; i <= numTransRows; i++) {
			Row<I> row = table.getRow(i);
			Word<I> rowPrefix = row.getPrefix();
			int prefixLen = rowPrefix.size();
			outputQueries.add(new DefaultQuery<I,Word<O>>(rowPrefix.prefix(prefixLen - 1),
					rowPrefix.suffix(1)));
		}
		
		oracle.processQueries(outputQueries);
		
		for(int i = 0; i < newOutputs; i++) {
			DefaultQuery<I,Word<O>> query = outputQueries.get(i);
			O outSym = query.getOutput().getSymbol(0);
			outputTable.add(outSym);
		}
	}

	@Override
	protected MealyMachine<?, I, ?, O> exposeInternalHypothesis() {
		return internalHyp;
	}
	
	@Override
	protected SuffixOutput<I,Word<O>> hypothesisOutput() {
		return internalHyp;
	}

}
