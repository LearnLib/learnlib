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
package de.learnlib.algorithms.lstargeneric.table;


/**
 * A description of an inconsistency in an {@link ObservationTable}. An inconsistency
 * consists of two short prefixes <code>u</code>, <code>u'</code> with identical contents,
 * and an input symbol <code>a</code>, such that the rows for <code>ua</code> and <code>u'a</code>
 * have different contents.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol class
 * @param <D> output class
 */
public final class Inconsistency<I, D> {
	private final Row<I> firstRow;
	private final Row<I> secondRow;
	private final int inputIndex;
	
	/**
	 * Constructor.
	 * @param firstRow the first row
	 * @param secondRow the second row
	 * @param inputIndex the input symbol for which the successor rows differ
	 */
	public Inconsistency(Row<I> firstRow, Row<I> secondRow, int inputIndex) {
		this.firstRow = firstRow;
		this.secondRow = secondRow;
		this.inputIndex = inputIndex;
	}
	
	/**
	 * Retrieves the first row.
	 * @return the first row
	 */
	public Row<I> getFirstRow() {
		return firstRow;
	}
	
	/**
	 * Retrieves the second row.
	 * @return the second row
	 */
	public Row<I> getSecondRow() {
		return secondRow;
	}
	
	/**
	 * Retrieves the index of the input symbol for which the successor rows differ.
	 * @return the input symbol index
	 */
	public int getInputIndex() {
		return inputIndex;
	}
}
