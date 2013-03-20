package de.learnlib.lstar;

import java.util.List;

import net.automatalib.automata.MutableDeterministic;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.lstar.closing.ClosingStrategy;
import de.learnlib.lstar.table.Row;

public abstract class ExtensibleAutomatonLStar<A,I,O,S,T,SP,TP,AI extends MutableDeterministic<S,I,T,SP,TP>> extends
AbstractAutomatonLStar<A, I, O,S,T,SP,TP,AI> {
	
	protected final ObservationTableCEXHandler<I, O> cexHandler;
	protected final ClosingStrategy<I, O> closingStrategy;
	protected final List<Word<I>> initialSuffixes;
	
	public ExtensibleAutomatonLStar(Alphabet<I> alphabet,
			MembershipOracle<I,O> oracle, AI internalHyp,
			List<Word<I>> initialSuffixes,
			ObservationTableCEXHandler<I,O> cexHandler,
			ClosingStrategy<I,O> closingStrategy) {
		super(alphabet, oracle, internalHyp);
		this.initialSuffixes = initialSuffixes;
		this.cexHandler = cexHandler;
		this.closingStrategy = closingStrategy;
	}
	
	@Override
	protected void doRefineHypothesis(Query<I, O> ceQuery) {
		List<List<Row<I>>> unclosed = cexHandler.handleCounterexample(ceQuery, table, oracle);
		completeConsistentTable(unclosed, cexHandler.needsConsistencyCheck());
	}
	
	@Override
	protected List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosed) {
		return closingStrategy.selectClosingRows(unclosed, table, oracle);
	}

	@Override
	protected List<Word<I>> initialSuffixes() {
		return initialSuffixes;
	}
	
	
}
