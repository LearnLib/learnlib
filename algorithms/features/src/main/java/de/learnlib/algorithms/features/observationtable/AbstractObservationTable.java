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

public abstract class AbstractObservationTable<I, D> implements ObservationTable<I, D> {

	public static abstract class AbstractRow<I,D> implements Row<I,D> {

		@Override
		public Iterator<D> iterator() {
			return Collections.unmodifiableCollection(getContents()).iterator();
		}

		@Override
		public int size() {
			return getContents().size();
		}
		
		@Override
		public D getCellContent(int index) {
			return getContents().get(index);
		}
		
	}
	
	public static class DefaultInconsistency<I,D> implements Inconsistency<I,D> {
		private final Row<I,D> firstRow;
		private final Row<I,D> secondRow;
		private final I symbol;
		
		public DefaultInconsistency(Row<I,D> firstRow, Row<I,D> secondRow, I symbol) {
			this.firstRow = firstRow;
			this.secondRow = secondRow;
			this.symbol = symbol;
		}
		
		@Override
		public Row<I,D> getFirstRow() {
			return firstRow;
		}
		
		@Override
		public Row<I,D> getSecondRow() {
			return secondRow;
		}
		
		@Override
		public I getSymbol() {
			return symbol;
		}
	}
	
	private final Function<Row<I,D>,Word<I>> getLabel
		= new Function<Row<I,D>,Word<I>>() {
		@Override
		public Word<I> apply(Row<I,D> row) {
			return row.getLabel();
		}
	};

	@Override
	public Collection<? extends Word<I>> getShortPrefixes() {
		Collection<? extends Row<I,D>> spRows = getShortPrefixRows();
		return Collections2.transform(spRows, getLabel);
	}
	
	@Override
	public Collection<? extends Word<I>> getLongPrefixes() {
		Collection<? extends Row<I,D>> lpRows = getLongPrefixRows();
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
	public List<? extends Row<I, D>> getAllRows() {
		Collection<? extends Row<I,D>> spRows = getShortPrefixRows();
		Collection<? extends Row<I,D>> lpRows = getLongPrefixRows();
		
		List<Row<I,D>> result = new ArrayList<>(spRows.size() + lpRows.size());
		result.addAll(spRows);
		result.addAll(lpRows);
		
		return result;
	}

	@Override
	public Row<I, D> getRow(Word<I> prefix) {
		for(Row<I,D> row : getAllRows()) {
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
	public Row<I, D> findUnclosedRow() {
		Set<List<? extends D>> spRowContents = new HashSet<>();
		for(Row<I,D> spRow : getShortPrefixRows()) {
			spRowContents.add(spRow.getContents());
		}
		
		for(Row<I,D> lpRow : getLongPrefixRows()) {
			if(!spRowContents.contains(lpRow.getContents())) {
				return lpRow;
			}
		}
		
		return null;
	}

	@Override
	public Inconsistency<I, D> findInconsistency(
			Collection<? extends I> inputs) {
		Map<List<? extends D>,Row<I,D>> spRowsByContent = new HashMap<>();
		for(Row<I,D> spRow : getShortPrefixRows()) {
			List<? extends D> content = spRow.getContents();
			Row<I,D> canonicalRow = spRowsByContent.get(content);
			if(canonicalRow != null) {
				for(I inputSym : inputs) {
					Row<I,D> spRowSucc = getSuccessorRow(spRow, inputSym);
					Row<I,D> canRowSucc = getSuccessorRow(canonicalRow, inputSym);
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
	public int findDistinguishingSuffixIndex(Inconsistency<I, D> inconsistency) {
		Row<I,D> row1 = inconsistency.getFirstRow();
		Row<I,D> row2 = inconsistency.getSecondRow();
		I sym = inconsistency.getSymbol();
		
		Row<I,D> succRow1 = getSuccessorRow(row1, sym);
		Row<I,D> succRow2 = getSuccessorRow(row2, sym);
		
		return findDistinguishingSuffixIndex(succRow1, succRow2);
	}
	
	@Override
	public Word<I> findDistinguishingSuffix(Inconsistency<I, D> inconsistency) {
		int suffixIndex = findDistinguishingSuffixIndex(inconsistency);
		if(suffixIndex != NO_DISTINGUISHING_SUFFIX) {
			return null;
		}
		return getSuffix(suffixIndex);
	}

	@Override
	public Word<I> findDistinguishingSuffix(Row<I, D> row1, Row<I, D> row2) {
		int suffixIndex = findDistinguishingSuffixIndex(row1, row2);
		if(suffixIndex != NO_DISTINGUISHING_SUFFIX) {
			return null;
		}
		return getSuffix(suffixIndex);
	}
	
	@Override
	public int findDistinguishingSuffixIndex(Row<I,D> row1, Row<I,D> row2) {
		Iterator<? extends D> values1It = row1.getContents().iterator();
		Iterator<? extends D> values2It = row2.getContents().iterator();
		
		int i = 0;
		while(values1It.hasNext() && values2It.hasNext()) {
			D value1 = values1It.next();
			D value2 = values2It.next();
			
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
