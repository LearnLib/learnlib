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
package de.learnlib.algorithms.lstargeneric.ce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.SuffixFinder;
import de.learnlib.oracles.DefaultQuery;

public class SuffixFinderCEXHandler<I, O> implements
		ObservationTableCEXHandler<I, O> {
	
	private final SuffixFinder<I, O> suffixFinder;
	
	public SuffixFinderCEXHandler(SuffixFinder<I,O> suffixFinder) {
		this.suffixFinder = suffixFinder;
	}

	@Override
	public List<List<Row<I>>> handleCounterexample(DefaultQuery<I, O> ceQuery,
			ObservationTable<I, O> table,
			SuffixOutput<I,O> hypOutput,
			MembershipOracle<I, O> oracle) {
		
		int suffixIdx = suffixFinder.findSuffixIndex(ceQuery, table, hypOutput, oracle);
		if(suffixIdx < 0)
			return Collections.emptyList();
	
		Word<I> qrySuffix = ceQuery.getSuffix();
		int suffixLen = qrySuffix.length();
		if(!suffixFinder.allSuffixes())
			return table.addSuffix(qrySuffix.subWord(suffixIdx, suffixLen), oracle);
		
		List<Word<I>> suffixes = new ArrayList<Word<I>>(suffixLen - suffixIdx);
		for(int i = suffixIdx; i < suffixLen; i++)
			suffixes.add(qrySuffix.subWord(i, suffixLen));
		
		return table.addSuffixes(suffixes, oracle);
	}

	@Override
	public boolean needsConsistencyCheck() {
		return false;
	}

}
