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
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class ShahbazCEXHandler<I, O> implements ObservationTableCEXHandler<I, O> {
	
	private static final ShahbazCEXHandler<?,?> INSTANCE
		= new ShahbazCEXHandler<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> ShahbazCEXHandler<I,O> getInstance() {
		return (ShahbazCEXHandler<I,O>)INSTANCE;
	}

	@Override
	public List<List<Row<I>>> handleCounterexample(DefaultQuery<I, O> ceQuery,
			ObservationTable<I, O> table, MembershipOracle<I,O> oracle) {
		Word<I> ceWord = ceQuery.getInput();
		
		int ceLen = ceWord.length();
		Row<I> row = table.getRow(0);
		int i = 0;
		while(i < ceLen) {
			I sym = ceWord.getSymbol(i++);
			row = table.getRowSuccessor(row, sym);
			
			if(!row.isShortPrefix())
				break;
		}
		
		int numSuffixes = ceLen - i;
		List<Word<I>> suffixes = new ArrayList<Word<I>>(numSuffixes);
		while(numSuffixes > 0)
			suffixes.add(ceWord.suffix(numSuffixes--));
		
		return table.addSuffixes(suffixes, oracle);
	}

	@Override
	public boolean needsConsistencyCheck() {
		return false;
	}

}
