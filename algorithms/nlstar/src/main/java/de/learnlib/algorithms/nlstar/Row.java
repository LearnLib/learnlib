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
package de.learnlib.algorithms.nlstar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.oracles.DefaultQuery;

/**
 * A single row in the {@link ObservationTable} for {@link NLStarLearner NL*}.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class Row<I> {
	
	private final Word<I> prefix;
	private int upperId = -1;
	
	private Row<I>[] successorRows;
	
	private final BitSet contents = new BitSet();
	
	// If this is a row in the upper part of the table,
	// this is the lists of all rows in the upper part that
	// are covered by this row.
	// Otherwise, this is the list of all rows in the whole
	// table that are covered by this row.
	private List<Row<I>> coveredRows = null;
	
	
	// Indicates if this row is prime. A row is prime if the join over
	// all rows in the coveredRows list
	private boolean prime = false;
	
	public Row(Word<I> prefix) {
		this.prefix = prefix;
	}
	
	public boolean getContent(int index) {
		return contents.get(index);
	}
	
	public boolean isNew() {
		return coveredRows == null;
	}
	
	
	public boolean isPrime() {
		return prime;
	}
	
	public int getUpperId() {
		return upperId;
	}
	
	public Word<I> getPrefix() {
		return prefix;
	}
	
	public boolean isShortPrefixRow() {
		return (successorRows != null);
	}
	
	public BitSet getContents() {
		return contents;
	}
	
	@SuppressWarnings("unchecked")
	void makeShort(int id, int alphabetSize) {
		this.upperId = id;
		this.successorRows = new Row[alphabetSize];
	}
	
	Row<I> getSuccessorRow(int succIdx) {
		return successorRows[succIdx];
	}
	
	void setSuccessorRow(int succIdx, Row<I> row) {
		successorRows[succIdx] = row;
	}

	
	void updateCovered(List<Row<I>> newRows) {
		List<Row<I>> oldCovered = coveredRows;
		
		this.coveredRows = new ArrayList<>();
		if(oldCovered != null) {
			checkAndAddCovered(oldCovered);
		}
		checkAndAddCovered(newRows);
	}
	
	private void checkAndAddCovered(List<Row<I>> rowList) {
		for(Row<I> row : rowList) {
			if(row != this) {
				if(isShortPrefixRow()) {
					if(row.isShortPrefixRow() && covers(row)) {
						coveredRows.add(row);
					}
				}
				else if(covers(row)) {
					coveredRows.add(row);
				}
			}
		}
	}
	
	public List<Row<I>> getCoveredRows() {
		return coveredRows;
	}

	
	boolean covers(Row<I> other) {
		BitSet c = (BitSet)contents.clone();
		c.or(other.contents);
		return contents.equals(c);
	}
	
	
	
	boolean checkPrime() {
		if(coveredRows.isEmpty()) {
			prime = true;
		}
		else {
			BitSet aggContents = new BitSet();
			
			for(Row<I> covered : coveredRows) {
				if(covered.isShortPrefixRow() || !contents.equals(covered.contents)) {
					aggContents.or(covered.contents);
				}
			}
			
			prime = !contents.equals(aggContents);
		}
		
		return prime;
	}
	
	void fetchContents(Iterator<? extends DefaultQuery<I,Boolean>> queryIt, int offset, int num) {
		int idx = offset;
		
		for(int i = 0; i < num; i++) {
			assert queryIt.hasNext();
			
			boolean value = queryIt.next().getOutput().booleanValue();
			if(value) {
				contents.set(idx);;
			}
			idx++;
		}
	}
}
