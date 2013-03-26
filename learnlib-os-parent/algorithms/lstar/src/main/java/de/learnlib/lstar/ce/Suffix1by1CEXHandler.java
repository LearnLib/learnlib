package de.learnlib.lstar.ce;

import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.table.ObservationTable;
import de.learnlib.lstar.table.Row;

public class Suffix1by1CEXHandler<I, O> implements
		ObservationTableCEXHandler<I, O> {
	
	private static final Suffix1by1CEXHandler<?,?> INSTANCE
		= new Suffix1by1CEXHandler<Object,Object>();
	
	@SuppressWarnings("unchecked")
	public static <I,O> Suffix1by1CEXHandler<I,O> getInstance() {
		return (Suffix1by1CEXHandler<I,O>)INSTANCE;
	}

	@Override
	public List<List<Row<I>>> handleCounterexample(Query<I, O> ceQuery,
			ObservationTable<I, O> table, MembershipOracle<I, O> oracle) {
		List<List<Row<I>>> unclosed = Collections.emptyList();
		
		List<Word<I>> suffixes = table.getSuffixes();
		
		Word<I> ceWord = ceQuery.getInput();
		int ceLen = ceWord.length();
		
		for(int i = 1; i <= ceLen; i++) {
			Word<I> suffix = ceWord.suffix(i);
			if(suffixes != null) {
				if(suffixes.contains(suffix))
					continue;
				suffixes = null;
			}
			
			unclosed = table.addSuffix(suffix, oracle);
			if(!unclosed.isEmpty())
				break;
		}
		
		return unclosed;
	}

	@Override
	public boolean needsConsistencyCheck() {
		return false;
	}

}
