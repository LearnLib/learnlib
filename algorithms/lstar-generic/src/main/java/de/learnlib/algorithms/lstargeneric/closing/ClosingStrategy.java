package de.learnlib.algorithms.lstargeneric.closing;

import java.util.List;

import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;

public interface ClosingStrategy<I, O> {
	public List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosedClasses, ObservationTable<I,O> table,
			MembershipOracle<I,O> oracle);
}
