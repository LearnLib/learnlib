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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.automatalib.words.Word;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

public abstract class AbstractObservationTable<I, O> implements ObservationTable<I, O> {

	public static abstract class AbstractRow<I,O> implements Row<I,O> {

		@Override
		public Iterator<O> iterator() {
			return Collections.unmodifiableCollection(getContents()).iterator();
		}

		@Override
		public int size() {
			return getContents().size();
		}
		
		@Override
		public O getCellContent(int index) {
			return getContents().get(index);
		}
		
	}
	
	public static class DefaultInconsistency<I,O> implements Inconsistency<I,O> {
		private final Row<I,O> firstRow;
		private final Row<I,O> secondRow;
		private final I symbol;
		
		public DefaultInconsistency(Row<I,O> firstRow, Row<I,O> secondRow, I symbol) {
			this.firstRow = firstRow;
			this.secondRow = secondRow;
			this.symbol = symbol;
		}
		
		@Override
		public Row<I,O> getFirstRow() {
			return firstRow;
		}
		
		@Override
		public Row<I,O> getSecondRow() {
			return secondRow;
		}
		
		@Override
		public I getSymbol() {
			return symbol;
		}
	}
	
	private final Function<Row<I,O>,Word<I>> getLabel
		= new Function<Row<I,O>,Word<I>>() {
		@Override
		public Word<I> apply(Row<I,O> row) {
			return row.getLabel();
		}
	};

	@Override
	public Collection<? extends Word<I>> getShortPrefixes() {
		Collection<? extends Row<I,O>> spRows = getShortPrefixRows();
		return Collections2.transform(spRows, getLabel);
	}
	
	@Override
	public Collection<? extends Word<I>> getLongPrefixes() {
		Collection<? extends Row<I,O>> lpRows = getLongPrefixRows();
		return Collections2.transform(lpRows, getLabel);
	}
	
	@Override
	public Collection<? extends Word<I>> getAllPrefixes() {
		Collection<? extends Word<I>> shortPrefixes = getShortPrefixes();
		Collection<? extends Word<I>> longPrefixes = getLongPrefixes();
		List<Word<I>> result = new ArrayList<>(shortPrefixes.size() + longPrefixes.size());
		
		result.addAll(shortPrefixes);
		result.addAll(longPrefixes);
		
		return result;
	}

	@Override
	public List<? extends Row<I, O>> getAllRows() {
		Collection<? extends Row<I,O>> spRows = getShortPrefixRows();
		Collection<? extends Row<I,O>> lpRows = getLongPrefixRows();
		
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
			spRowContents.add(spRow.getContents());
		}
		
		for(Row<I,O> lpRow : getLongPrefixRows()) {
			if(!spRowContents.contains(lpRow.getContents())) {
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
			List<? extends O> content = spRow.getContents();
			Row<I,O> canonicalRow = spRowsByContent.get(content);
			if(canonicalRow != null) {
				for(I inputSym : inputs) {
					Row<I,O> spRowSucc = getSuccessorRow(spRow, inputSym);
					Row<I,O> canRowSucc = getSuccessorRow(canonicalRow, inputSym);
					if(spRowSucc != canRowSucc) {
						if(!spRowSucc.getContents().equals(canRowSucc.getContents())) {
							return new DefaultInconsistency<>(spRow, canonicalRow, inputSym);
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
	public Word<I> getSuffix(int index) {
		return getSuffixes().get(index);
	}
	
	@Override
	public int findDistinguishingSuffixIndex(Inconsistency<I, O> inconsistency) {
		Row<I,O> row1 = inconsistency.getFirstRow();
		Row<I,O> row2 = inconsistency.getSecondRow();
		I sym = inconsistency.getSymbol();
		
		Row<I,O> succRow1 = getSuccessorRow(row1, sym);
		Row<I,O> succRow2 = getSuccessorRow(row2, sym);
		
		return findDistinguishingSuffixIndex(succRow1, succRow2);
	}
	
	@Override
	public Word<I> findDistinguishingSuffix(Inconsistency<I, O> inconsistency) {
		int suffixIndex = findDistinguishingSuffixIndex(inconsistency);
		if(suffixIndex != NO_DISTINGUISHING_SUFFIX) {
			return null;
		}
		return getSuffix(suffixIndex);
	}

	@Override
	public Word<I> findDistinguishingSuffix(Row<I, O> row1, Row<I, O> row2) {
		int suffixIndex = findDistinguishingSuffixIndex(row1, row2);
		if(suffixIndex != NO_DISTINGUISHING_SUFFIX) {
			return null;
		}
		return getSuffix(suffixIndex);
	}
	
	@Override
	public int findDistinguishingSuffixIndex(Row<I,O> row1, Row<I,O> row2) {
		Iterator<? extends O> values1It = row1.getContents().iterator();
		Iterator<? extends O> values2It = row2.getContents().iterator();
		
		int i = 0;
		while(values1It.hasNext() && values2It.hasNext()) {
			O value1 = values1It.next();
			O value2 = values2It.next();
			
			if(!Objects.equal(value1, value2)) {
				return i;
			}
			i++;
		}
		
		if(values1It.hasNext() || values2It.hasNext()) {
			throw new IllegalStateException("Rows [" + row1.getLabel() + "] and/or [" + row2.getLabel() + "] have invalid length");
		}
		
		return NO_DISTINGUISHING_SUFFIX;
	}
	
	@Override
	public boolean isConsistent(Collection<? extends I> inputs) {
		return (findInconsistency(inputs) == null);
	}

}
