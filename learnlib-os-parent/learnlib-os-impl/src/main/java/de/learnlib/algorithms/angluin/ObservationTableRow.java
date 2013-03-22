package de.learnlib.algorithms.angluin;

import java.util.LinkedList;
import java.util.List;

class ObservationTableRow {

	private final List<Boolean> rowData;

	ObservationTableRow() {
		rowData = new LinkedList<>();
	}

	void addValue(boolean value) {
		rowData.add(value);
	}

	List<Boolean> getValues() {
		return rowData;
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
