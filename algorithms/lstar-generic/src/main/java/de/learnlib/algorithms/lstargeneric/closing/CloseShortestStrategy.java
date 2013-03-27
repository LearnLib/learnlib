package de.learnlib.algorithms.lstargeneric.closing;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;

public class CloseShortestStrategy<I, O> implements ClosingStrategy<I, O> {
	
	private static final CloseShortestStrategy<?,?> INSTANCE
		= new CloseShortestStrategy<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> CloseShortestStrategy<I,O> getInstance() {
		return (CloseShortestStrategy<I,O>)INSTANCE;
	}

	@Override
	public List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosedClasses,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		List<Row<I>> result = new ArrayList<Row<I>>();
		
		
		for(List<Row<I>> clazz : unclosedClasses) {
			Row<I> shortest = null;
			int shortestLen = Integer.MAX_VALUE;
			
			for(Row<I> row : clazz) {
				int prefixLen = row.getPrefix().length();
				if(shortest == null || prefixLen < shortestLen) {
					shortest = row;
					shortestLen = prefixLen;
				}
			}
			
			result.add(shortest);
		}
		
		return result;
	}

}
