package de.learnlib.oracles;

import java.util.Collection;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.QueryAnswerer;

public class QueryAnswererOracle<I, O> implements MembershipOracle<I,O> {
	
	private final QueryAnswerer<I,O> answerer;

	public QueryAnswererOracle(QueryAnswerer<I,O> answerer) {
		this.answerer = answerer;
	}

	@Override
	public void processQueries(Collection<? extends Query<I, O>> queries) {
		MQUtil.answerQueries(answerer, queries);
	}
	
}
