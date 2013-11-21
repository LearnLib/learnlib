package de.learnlib.filters.reuse;

import net.automatalib.words.Word;
import de.learnlib.filters.reuse.tree.ReuseNode;

/**
 * TODO JavaDoc.
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 *
 * @param <S>
 * @param <I>
 * @param <O>
 */
public interface ReuseCapableOracle<S,I,O> {
	public static final class QueryResult<S, O> {
		public final Word<O> output;
		public final S newState;
		public final boolean oldInvalidated;
		public QueryResult(Word<O> output, S newState, boolean oldInvalidated) {
			super();
			this.output = output;
			this.newState = newState;
			this.oldInvalidated = oldInvalidated;
		}
	}
	
	/**
	 * 
	 * @param trace
	 * @param s
	 * @return
	 */
	QueryResult<S, O> continueQuery(Word<I> trace, ReuseNode<S, I, O> s);
	
	/**
	 * Implementation needs to provide a fresh system state, process the whole
	 * query and return a {@link QueryResult} with the resulting system state,
	 * the SUL output to that query. The {@link QueryResult#oldInvalidated} will
	 * not be interpreted.
	 * 
	 * @param trace
	 * @return
	 */
	QueryResult<S, O> processQuery(Word<I> trace);
}
