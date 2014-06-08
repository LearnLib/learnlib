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

/**
 * An (RFSA) inconsistency in an {@link ObservationTable}.
 *  
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 */
public class Inconsistency<I> {
	
	private final Row<I> row1;
	private final Row<I> row2;
	
	private final int symbolIdx;
	
	private final int suffixIdx;
	
	/**
	 * Constructor.
	 * @param row1 the first upper prime row
	 * @param row2 the second upper prime row (covered by {@code row1})
	 * @param symbolIdx the index of the symbol
	 * @param suffixIdx the index of the suffix
	 */
	public Inconsistency(Row<I> row1, Row<I> row2, int symbolIdx, int suffixIdx) {
		this.row1 = row1;
		this.row2 = row2;
		this.symbolIdx = symbolIdx;
		this.suffixIdx = suffixIdx;
	}

	public Row<I> getRow1() {
		return row1;
	}

	public Row<I> getRow2() {
		return row2;
	}

	public int getSymbolIdx() {
		return symbolIdx;
	}

	public int getSuffixIdx() {
		return suffixIdx;
	}

	
	
}
