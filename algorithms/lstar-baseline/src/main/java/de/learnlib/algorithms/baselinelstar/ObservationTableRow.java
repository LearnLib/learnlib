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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A single row inside an {@link ObservationTable}, containing only the boolean values
 * if a combination of state/candidate and suffix is accepted by the current hypothesis.
 */
class ObservationTableRow {

	private final List<Boolean> rowData;

	ObservationTableRow() {
		rowData = new LinkedList<>();
	}

	void addValue(boolean value) {
		rowData.add(value);
	}

	void clear() {
		rowData.clear();
	}

	List<Boolean> getValues() {
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

		ObservationTableRow that = (ObservationTableRow) o;

		return rowData.equals(that.rowData);
	}

	@Override
	public int hashCode() {
		return rowData.hashCode();
	}

	@Override
	public String toString() {
		return rowData.toString();
	}

}
