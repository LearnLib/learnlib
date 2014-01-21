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

import java.util.Collection;
import java.util.List;

import net.automatalib.words.Word;

public interface ObservationTable<I, O> {
	
	public static interface Row<I,O> extends Iterable<O> {
		public Word<I> getLabel();
		public boolean isShortPrefixRow();
		
		public List<O> getValues();
		public int size();
		
		public O getValue(int index);
	}
	
	public static final class Inconsistency<I,O> {
		private final Row<I,O> firstRow;
		private final Row<I,O> secondRow;
		private final I symbol;
		
		public Inconsistency(Row<I,O> firstRow, Row<I,O> secondRow, I symbol) {
			this.firstRow = firstRow;
			this.secondRow = secondRow;
			this.symbol = symbol;
		}
		
		public Row<I,O> getFirstRow() {
			return firstRow;
		}
		
		public Row<I,O> getSecondRow() {
			return secondRow;
		}
		
		public I getSymbol() {
			return symbol;
		}
	}
	
	public List<? extends Word<I>> getShortPrefixes();
	public List<? extends Word<I>> getLongPrefixes();
	
	public List<? extends Word<I>> getAllPrefixes();
	
	public List<? extends Word<I>> getSuffixes();
	
	public List<? extends Row<I,O>> getShortPrefixRows();
	public List<? extends Row<I,O>> getLongPrefixRows();
	public List<? extends Row<I,O>> getAllRows();
	
	public Row<I,O> getRow(Word<I> prefix);				
	
	public Row<I,O> getSuccessorRow(Row<I,O> spRow, I symbol);
	
	public boolean isClosed();
	public Row<I,O> findUnclosedRow();
	
	public Inconsistency<I,O> findInconsistency(Collection<? extends I> inputs);
	public Word<I> getDistinguishingSuffix(Row<I,O> row1, Row<I,O> row2);	
}
