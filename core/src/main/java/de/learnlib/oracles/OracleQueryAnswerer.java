package de.learnlib.oracles;

import java.util.Collections;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.QueryAnswerer;

public final class OracleQueryAnswerer<I, O> implements QueryAnswerer<I,O> {
	
	private final MembershipOracle<I,O> oracle;
	
	public OracleQueryAnswerer(MembershipOracle<I,O> oracle) {
		this.oracle = oracle;
	}

	@Override
	public O answerQuery(Word<I> prefix, Word<I> suffix) {
		DefaultQuery<I,O> query = new DefaultQuery<>(prefix, suffix);
		oracle.processQueries(Collections.singleton(query));
		return query.getOutput();
	}

}
