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
package de.learnlib.algorithms.baselinelstar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import de.learnlib.algorithms.features.observationtable.AbstractObservationTable;

import net.automatalib.words.Word;

/**
 * A single row inside an {@link ObservationTable}, containing only the boolean values
 * if a combination of state/candidate and suffix is accepted by the current hypothesis.
 */
class ObservationTableRow<I> extends AbstractObservationTable.AbstractRow<I, Boolean> {

	@Nonnull
	private final Word<I> label;

	@Nonnull
	private final List<Boolean> rowData;

	private boolean shortPrefixRow;

	ObservationTableRow(@Nonnull Word<I> label) {
		this.label = label;
		rowData = new ArrayList<>();
	}

	void addValue(Boolean value) {
		rowData.add(value);
	}

	void clear() {
		rowData.clear();
	}

	void setShortPrefixRow() {
		shortPrefixRow = true;
	}

	void setLongPrefixRow() {
		shortPrefixRow = false;
	}

	@Override
	@Nonnull
	public Word<I> getLabel() {
		return label;
	}

	@Override
	public boolean isShortPrefixRow() {
		return shortPrefixRow;
	}

	@Override
	@Nonnull
	public List<Boolean> getContents() {
		return Collections.unmodifiableList(rowData);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof ObservationTableRow)) {
			return false;
		}

		ObservationTableRow<?> that = (ObservationTableRow<?>) o;

		return label.equals(that.label) && rowData.equals(that.rowData);
	}

	@Override
	public int hashCode() {
		return 7 * rowData.hashCode() + 13 * label.hashCode();
	}

	@Override
	public String toString() {
		return label.toString() + ": " + rowData.toString();
	}

	boolean isContentsEqual(ObservationTableRow<I> row) {
		return rowData.equals(row.rowData);
	}

}
