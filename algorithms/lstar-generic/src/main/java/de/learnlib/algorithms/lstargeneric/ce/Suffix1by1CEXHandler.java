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

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class Suffix1by1CEXHandler<I, O> implements
		ObservationTableCEXHandler<I, O> {
	
	private static final Suffix1by1CEXHandler<?,?> INSTANCE
		= new Suffix1by1CEXHandler<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> Suffix1by1CEXHandler<I,O> getInstance() {
		return (Suffix1by1CEXHandler<I,O>)INSTANCE;
	}

	@Override
	public List<List<Row<I>>> handleCounterexample(DefaultQuery<I, O> ceQuery,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		List<List<Row<I>>> unclosed = Collections.emptyList();
		
		List<Word<I>> suffixes = table.getSuffixes();
		
		Word<I> ceWord = ceQuery.getInput();
		int ceLen = ceWord.length();
		
		for(int i = 1; i <= ceLen; i++) {
			Word<I> suffix = ceWord.suffix(i);
			if(suffixes != null) {
				if(suffixes.contains(suffix))
					continue;
				suffixes = null;
			}
			
			unclosed = table.addSuffix(suffix, oracle);
			if(!unclosed.isEmpty())
				break;
		}
		
		return unclosed;
	}

	@Override
	public boolean needsConsistencyCheck() {
		return false;
	}

}
