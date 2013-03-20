package de.learnlib.lstar.closing;

import java.util.List;

import de.learnlib.api.MembershipOracle;
import de.learnlib.lstar.table.ObservationTable;
import de.learnlib.lstar.table.Row;

public interface ClosingStrategy<I, O> {
	public List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosedClasses, ObservationTable<I,O> table,
			MembershipOracle<I,O> oracle);
}
