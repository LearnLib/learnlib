/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.algorithms.features.observationtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.words.Word;

import com.google.common.base.Objects;

public abstract class AbstractObservationTable<I, O> implements ObservationTable<I, O> {
	
	public static abstract class AbstractRow<I,O> implements Row<I,O> {

		@Override
		public Iterator<O> iterator() {
			return getValues().iterator();
		}

		@Override
		public int size() {
			return getValues().size();
		}
		
		@Override
		public O getValue(int index) {
			return getValues().get(index);
		}
		
	}

	@Override
	public List<? extends Word<I>> getShortPrefixes() {
		List<? extends Row<I,O>> spRows = getShortPrefixRows();
		List<Word<I>> result = new ArrayList<>(spRows.size());
		for(Row<I,O> row : spRows) {
			result.add(row.getLabel());
		}
		return result;
	}
	
	@Override
	public List<? extends Word<I>> getLongPrefixes() {
		List<? extends Row<I,O>> lpRows = getLongPrefixRows();
		List<Word<I>> result = new ArrayList<>(lpRows.size());
		for(Row<I,O> row : lpRows) {
			result.add(row.getLabel());
		}
		return result;
	}
	
	@Override
	public List<? extends Word<I>> getAllPrefixes() {
		List<? extends Word<I>> shortPrefixes = getShortPrefixes();
		List<? extends Word<I>> longPrefixes = getLongPrefixes();
		List<Word<I>> result = new ArrayList<>(shortPrefixes.size() + longPrefixes.size());
		
		result.addAll(shortPrefixes);
		result.addAll(longPrefixes);
		
		return result;
	}

	@Override
	public List<Row<I, O>> getAllRows() {
		List<? extends Row<I,O>> spRows = getShortPrefixRows();
		List<? extends Row<I,O>> lpRows = getLongPrefixRows();
		
		List<Row<I,O>> result = new ArrayList<>(spRows.size() + lpRows.size());
		result.addAll(spRows);
		result.addAll(lpRows);
		
		return result;
	}

	@Override
	public Row<I, O> getRow(Word<I> prefix) {
		for(Row<I,O> row : getAllRows()) {
			if(prefix.equals(row.getLabel())) {
				return row;
			}
		}
		
		return null;
	}

	@Override
	public boolean isClosed() {
		return (findUnclosedRow() == null);
	}

	@Override
	public Row<I, O> findUnclosedRow() {
		Set<List<? extends O>> spRowContents = new HashSet<>();
		for(Row<I,O> spRow : getShortPrefixRows()) {
			spRowContents.add(spRow.getValues());
		}
		
		for(Row<I,O> lpRow : getLongPrefixRows()) {
			if(!spRowContents.contains(lpRow.getValues())) {
				return lpRow;
			}
		}
		
		return null;
	}

	@Override
	public Inconsistency<I, O> findInconsistency(
			Collection<? extends I> inputs) {
		Map<List<? extends O>,Row<I,O>> spRowsByContent = new HashMap<>();
		for(Row<I,O> spRow : getShortPrefixRows()) {
			List<? extends O> content = spRow.getValues();
			Row<I,O> canonicalRow = spRowsByContent.get(content);
			if(canonicalRow != null) {
				for(I inputSym : inputs) {
					Row<I,O> spRowSucc = getSuccessorRow(spRow, inputSym);
					Row<I,O> canRowSucc = getSuccessorRow(canonicalRow, inputSym);
					if(spRowSucc != canRowSucc) {
						if(!spRowSucc.getValues().equals(canRowSucc.getValues())) {
							return new Inconsistency<>(spRow, canonicalRow, inputSym);
						}
					}
				}
			}
			else {
				spRowsByContent.put(content, spRow);
			}
		}
		
		return null;
	}

	@Override
	public Word<I> getDistinguishingSuffix(Row<I, O> row1, Row<I, O> row2) {
		Iterator<? extends O> values1It = row1.getValues().iterator();
		Iterator<? extends O> values2It = row2.getValues().iterator();
		
		Iterator<? extends Word<I>> suffixIt = getSuffixes().iterator();
		while(suffixIt.hasNext() && values1It.hasNext() && values2It.hasNext()) {
			Word<I> suffix = suffixIt.next();
			O value1 = values1It.next();
			O value2 = values2It.next();
			
			if(!Objects.equal(value1, value2)) {
				return suffix;
			}
		}
		
		if(suffixIt.hasNext() || values1It.hasNext() || values2It.hasNext()) {
			throw new IllegalStateException("Rows [" + row1.getLabel() + "] and/or [" + row2.getLabel() + "] have invalid length");
		}
		
		return null;
	}

}
