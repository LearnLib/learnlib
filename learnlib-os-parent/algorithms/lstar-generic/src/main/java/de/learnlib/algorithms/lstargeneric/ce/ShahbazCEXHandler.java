package de.learnlib.algorithms.lstargeneric.ce;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

public class ShahbazCEXHandler<I, O> implements ObservationTableCEXHandler<I, O> {
	
	private static final ShahbazCEXHandler<?,?> INSTANCE
		= new ShahbazCEXHandler<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> ShahbazCEXHandler<I,O> getInstance() {
		return (ShahbazCEXHandler<I,O>)INSTANCE;
	}

	@Override
	public List<List<Row<I>>> handleCounterexample(Query<I, O> ceQuery,
			ObservationTable<I, O> table, MembershipOracle<I,O> oracle) {
		Word<I> ceWord = ceQuery.getInput();
		
		int ceLen = ceWord.length();
		Row<I> row = table.getRow(0);
		int i = 0;
		while(i < ceLen) {
			I sym = ceWord.getSymbol(i++);
			row = table.getRowSuccessor(row, sym);
			
			if(!row.isShortPrefix())
				break;
		}
		
		int numSuffixes = ceLen - i;
		List<Word<I>> suffixes = new ArrayList<Word<I>>(numSuffixes);
		while(numSuffixes > 0)
			suffixes.add(ceWord.suffix(numSuffixes--));
		
		return table.addSuffixes(suffixes, oracle);
	}

	@Override
	public boolean needsConsistencyCheck() {
		return false;
	}

}
