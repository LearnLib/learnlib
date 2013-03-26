package de.learnlib.algorithms.lstargeneric.ce;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

public class ClassicLStarCEXHandler<I, O> implements
		ObservationTableCEXHandler<I, O> {
	
	private static final ClassicLStarCEXHandler<?,?> INSTANCE
		= new ClassicLStarCEXHandler<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> ClassicLStarCEXHandler<I,O> getInstance() {
		return (ClassicLStarCEXHandler<I,O>)INSTANCE;
	}

	@Override
	public List<List<Row<I>>> handleCounterexample(DefaultQuery<I, O> ceQuery,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		
		Word<I> ceWord = ceQuery.getInput();
		
		List<Word<I>> newPrefixes = new ArrayList<Word<I>>(ceWord.size() - 2);
		
		for(int i = 1; i <= ceWord.size(); i++) {
			Word<I> prefix = ceWord.prefix(i);
			newPrefixes.add(prefix);
		}
		
		return table.addShortPrefixes(newPrefixes, oracle);
	}

	@Override
	public boolean needsConsistencyCheck() {
		return true;
	}

}
