package de.learnlib.filters.reuse;

import net.automatalib.words.Word;

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
		public QueryResult(Word<O> output, S newState) {
			super();
			this.output = output;
			this.newState = newState;
		}
	}
	
	/**
	 * 
	 * @param trace
	 * @param s
	 * @return
	 */
	QueryResult<S, O> continueQuery(Word<I> trace, S s);
	
	/**
	 * An implementation needs to provide a fresh system state, process the whole
	 * query and return a {@link QueryResult} with the resulting system state 
	 * ({@link QueryResult#newState}) and the SUL output to that query 
	 * ({@link QueryResult#output}).
	 * 
	 * @param trace
	 * @return
	 */
	QueryResult<S, O> processQuery(Word<I> trace);
}
