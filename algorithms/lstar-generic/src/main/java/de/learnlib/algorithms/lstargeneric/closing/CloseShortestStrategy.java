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
package de.learnlib.algorithms.lstargeneric.closing;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;

public class CloseShortestStrategy<I, O> implements ClosingStrategy<I, O> {
	
	private static final CloseShortestStrategy<?,?> INSTANCE
		= new CloseShortestStrategy<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> CloseShortestStrategy<I,O> getInstance() {
		return (CloseShortestStrategy<I,O>)INSTANCE;
	}

	@Override
	public List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosedClasses,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		List<Row<I>> result = new ArrayList<Row<I>>();
		
		
		for(List<Row<I>> clazz : unclosedClasses) {
			Row<I> shortest = null;
			int shortestLen = Integer.MAX_VALUE;
			
			for(Row<I> row : clazz) {
				int prefixLen = row.getPrefix().length();
				if(shortest == null || prefixLen < shortestLen) {
					shortest = row;
					shortestLen = prefixLen;
				}
			}
			
			result.add(shortest);
		}
		
		return result;
	}

}
