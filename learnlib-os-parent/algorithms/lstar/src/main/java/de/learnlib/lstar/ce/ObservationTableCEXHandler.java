package de.learnlib.lstar.ce;

import java.util.List;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.table.ObservationTable;
import de.learnlib.lstar.table.Row;

public interface ObservationTableCEXHandler<I, O> {
	public List<List<Row<I>>> handleCounterexample(Query<I,O> ceQuery,
			ObservationTable<I,O> table, MembershipOracle<I,O> oracle);
	
	public boolean needsConsistencyCheck();
}
