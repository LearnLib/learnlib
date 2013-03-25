package de.learnlib.lstar.closing;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.MembershipOracle;
import de.learnlib.lstar.table.ObservationTable;
import de.learnlib.lstar.table.Row;

public class CloseFirstStrategy<I, O> implements ClosingStrategy<I, O> {

	private static final CloseFirstStrategy<?,?> INSTANCE
		= new CloseFirstStrategy<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> CloseFirstStrategy<I,O> getInstance() {
		return (CloseFirstStrategy<I,O>)INSTANCE;
	}
	
	@Override
	public List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosedClasses,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		List<Row<I>> result = new ArrayList<Row<I>>(unclosedClasses.size());
		for(List<Row<I>> clazz : unclosedClasses)
			result.add(clazz.get(0));
		
		return result;
	}

}
