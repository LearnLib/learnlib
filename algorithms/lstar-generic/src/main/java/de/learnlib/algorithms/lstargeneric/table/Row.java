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
package de.learnlib.algorithms.lstargeneric.table;

import net.automatalib.words.Word;


/**
 * A row in an observation table. Minimally, a row consists of a prefix (the row label)
 * and a unique identifier in its observation table which remains constant throughout the
 * whole process.
 * <p>
 * Apart from that, a row is also associated with contents (via an integer id). The prefix of a row
 * may be either a short or long prefix. In the former case, the row will also have successor rows
 * (one-step futures) associated with it.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class
 */
public final class Row<I> {
	private final Word<I> prefix;
	private final int rowId;
	
	private int rowContentId = -1;
	private int lpIndex = 0;
	private Row<I>[] successors = null; 
	
	/**
	 * Constructor.
	 * @param prefix the prefix (label) of this row 
	 * @param rowId the unique row identifier
	 */
	public Row(Word<I> prefix, int rowId) {
		this.prefix = prefix;
		this.rowId = rowId;
	}
	
	/**
	 * Constructor for short prefix rows. 
	 * @param prefix the prefix (label) of this row
	 * @param rowId the unique row identifier
	 * @param alphabetSize the size of the alphabet, used for initializing the successor array
	 */
	public Row(Word<I> prefix, int rowId, int alphabetSize) {
		this(prefix, rowId);
		
		makeShort(alphabetSize);
	}
	
	/**
	 * Makes this row a short prefix row. This leads to a successor array being created.
	 * If this row already is a short prefix row, nothing happens.
	 * @param alphabetSize the size of the input alphabet.
	 */
	@SuppressWarnings("unchecked")
	public void makeShort(int alphabetSize) {
		if(lpIndex == -1)
			return;
		lpIndex = -1;
		this.successors = (Row<I>[])new Row<?>[alphabetSize];
	}
	
	/**
	 * Retrieves the successor row for this short prefix row and the given alphabet
	 * symbol (by index). If this is no short prefix row, an exception might occur.
	 * @param inputIdx the index of the alphabet symbol.
	 * @return the successor row (may be <code>null</code>)
	 */
	public Row<I> getSuccessor(int inputIdx) {
		return successors[inputIdx];
	}
	
	/**
	 * Sets the successor row for this short prefix row and the given alphabet symbol
	 * (by index). If this is no short prefix row, an exception might occur.
	 * @param inputIdx the index of the alphabet symbol.
	 * @param succ the successor row
	 */
	public void setSuccessor(int inputIdx, Row<I> succ) {
		successors[inputIdx] = succ;
	}
	
	/**
	 * Retrieves the prefix (row label) associated with this row.
	 * @return the prefix
	 */
	public Word<I> getPrefix() {
		return prefix;
	}
	
	/**
	 * Retrieves the unique row identifier associated with this row.
	 * @return the row identifier
	 */
	public int getRowId() {
		return rowId;
	}
	
	/**
	 * Retrieves the ID of the row contents (may be <code>-1</code> if this row has not
	 * yet been initialized).
	 * @return the contents id.
	 */
	public int getRowContentId() {
		return rowContentId;
	}
	
	/**
	 * Sets the ID of the row contents.
	 * @param id the contents id
	 */
	public void setRowContentId(int id) {
		this.rowContentId = id;
	}
	
	/**
	 * Retrieves whether this is a short prefix row.
	 * @return <code>true</code> if this is a short prefix row, <code>false</code> otherwise.
	 */
	public boolean isShortPrefix() {
		return (lpIndex == -1);
	}

	public boolean hasContents() {
		return (rowContentId != -1);
	}
	
	int getLpIndex() {
		return lpIndex;
	}
	
	void setLpIndex(int lpIndex) {
		this.lpIndex = lpIndex;
	}
	
}
