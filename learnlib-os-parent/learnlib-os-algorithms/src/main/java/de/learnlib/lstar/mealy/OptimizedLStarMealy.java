package de.learnlib.lstar.mealy;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.automata.transout.impl.MealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.util.Words;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.AbstractAutomatonLStar;
import de.learnlib.lstar.table.Row;

public class OptimizedLStarMealy<I, O> extends
		AbstractAutomatonLStar<MealyMachine<?,I,?,O>, I, Word<O>, FastMealyState<O>, MealyTransition<FastMealyState<O>,O>, Void, O, FastMealy<I,O>> {
	
	private final List<O> outputTable
		= new ArrayList<O>();
	
	private final List<Word<I>> initialSuffixes;

	public OptimizedLStarMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle, List<Word<I>> initialSuffixes) {
		super(alphabet, oracle, new FastMealy<I,O>(alphabet));
		this.initialSuffixes = new ArrayList<Word<I>>(initialSuffixes);
	}

	@Override
	protected Void stateProperty(Row<I> stateRow) {
		return null;
	}

	@Override
	protected O transitionProperty(Row<I> stateRow, int inputIdx) {
		updateOutputs();
		Row<I> transRow = stateRow.getSuccessor(inputIdx);
		return outputTable.get(transRow.getRowId() - 1);
	}

	@Override
	protected List<Word<I>> initialSuffixes() {
		return initialSuffixes;
	}
	
	protected void updateOutputs() {
		int numOutputs = outputTable.size();
		int numTransRows = table.numTotalRows() - 1;
		
		int newOutputs = numTransRows - numOutputs;
		if(newOutputs == 0)
			return;
		
		List<Query<I,Word<O>>> outputQueries
			= new ArrayList<Query<I,Word<O>>>(numOutputs);
		
		for(int i = numOutputs+1; i <= numTransRows; i++) {
			Row<I> row = table.getRow(i);
			Word<I> rowPrefix = row.getPrefix();
			int prefixLen = rowPrefix.size();
			outputQueries.add(new Query<I,Word<O>>(Words.prefix(rowPrefix, prefixLen - 1),
					Words.suffix(rowPrefix, 1)));
		}
		
		oracle.processQueries(outputQueries);
		
		for(int i = 0; i < newOutputs; i++) {
			Query<I,Word<O>> query = outputQueries.get(i);
			O outSym = query.getOutput().getSymbol(0);
			outputTable.add(outSym);
		}
	}

	@Override
	protected MealyMachine<?, I, ?, O> exposeInternalHypothesis() {
		return internalHyp;
	}

}
