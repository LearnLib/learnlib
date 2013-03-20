package de.learnlib.lstar.dfa;

import java.util.Collections;
import java.util.List;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.AbstractAutomatonLStar;
import de.learnlib.lstar.table.Row;

public class Suffix1by1DFA<I> extends
		AbstractAutomatonLStar<DFA<?,I>, I, Boolean, Integer, Integer, Boolean, Void, CompactDFA<I>> {

	public Suffix1by1DFA(Alphabet<I> alphabet,
			MembershipOracle<I, Boolean> oracle) {
		super(alphabet, oracle, new CompactDFA<I>(alphabet));
	}

	@Override
	protected Boolean stateProperty(Row<I> stateRow) {
		System.err.println("Property for row [" + stateRow.getPrefix() + "] is " + table.cellContents(stateRow, 0));
		return table.cellContents(stateRow, 0);
	}

	@Override
	protected Void transitionProperty(Row<I> stateRow, int inputIdx) {
		return null;
	}

	@Override
	protected DFA<?, I> exposeInternalHypothesis() {
		return internalHyp;
	}

	@Override
	protected List<Word<I>> initialSuffixes() {
		return Collections.singletonList(Word.<I>epsilon()); 
	}

	@Override
	protected List<List<Row<I>>> incorporateCounterExample(Query<I, Boolean> ce) {
		Word<I> ceWord = ce.getInput();
		int ceLen = ceWord.length();
		
		List<List<Row<I>>> unclosed = Collections.emptyList();
		List<Word<I>> suffixes = table.getSuffixes();
		System.err.println("Table contains suffixes " + suffixes);
		for(int i = 1; i <= ceLen; i++) {
			Word<I> suff = ceWord.suffix(i);
			if(suffixes.contains(suff)) {
				System.err.println("Skipping suffix " + suff + " as is already present");
				continue;
			}
			
			unclosed = table.addSuffix(suff, oracle);
			if(unclosed != null && !unclosed.isEmpty()) {
				System.err.println("Suffix " + suff + " did the trick");
				break;
			}
		}
		
		return unclosed;
	}
	
	

}
