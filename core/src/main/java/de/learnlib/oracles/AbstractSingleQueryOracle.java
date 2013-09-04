package de.learnlib.oracles;

import java.util.Collection;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.QueryAnswerer;

public abstract class AbstractSingleQueryOracle<I, O> implements MembershipOracle<I, O>, QueryAnswerer<I, O> {

	public AbstractSingleQueryOracle() {
	}

	/* (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, O>> queries) {
		MQUtil.answerQueries(this, queries);
	}
	
}
