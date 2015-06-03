/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.baselinelstar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.automatalib.words.Word;

/**
 * A single row inside an {@link ObservationTable}, containing only the boolean values
 * if a combination of state/candidate and suffix is accepted by the current hypothesis.
 */
class ObservationTableRow<I> extends ObservationTable.AbstractRow<I, Boolean> {

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
