package de.learnlib.algorithms.lstargeneric.closing;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;

public class CloseLexMinStrategy<I, O> implements ClosingStrategy<I, O> {

	private static final CloseLexMinStrategy<?,?> INSTANCE
		= new CloseLexMinStrategy<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> CloseLexMinStrategy<I,O> getInstance() {
		return (CloseLexMinStrategy<I,O>)INSTANCE;
	}
	
	@Override
	public List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosedClasses,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		List<Row<I>> result = new ArrayList<Row<I>>(unclosedClasses.size());
		
		Alphabet<I> alphabet = table.getInputAlphabet();
		
		for(List<Row<I>> clazz : unclosedClasses) {
			Row<I> lexMin = null;
			
			for(Row<I> row : clazz) {
				if(lexMin == null)
					lexMin = row;
				else if(CmpUtil.lexCompare(row.getPrefix(), lexMin.getPrefix(), alphabet) < 0)
					lexMin = row;
			}
			
			result.add(lexMin);
		}
		
		return result;
	}

}
