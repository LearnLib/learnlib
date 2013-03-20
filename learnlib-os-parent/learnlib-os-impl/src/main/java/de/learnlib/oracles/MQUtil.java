package de.learnlib.oracles;

import java.util.Collections;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

public abstract class MQUtil {
	
	public static <I,O> O query(MembershipOracle<I,O> oracle, Word<I> queryWord) {
		Query<I,O> qry = new Query<>(queryWord);
		oracle.processQueries(Collections.singleton(qry));
		return qry.getOutput();
	}
	
	public static <I,O> O query(MembershipOracle<I,O> oracle, Word<I> prefix, Word<I> suffix) {
		Query<I,O> qry = new Query<>(prefix, suffix);
		oracle.processQueries(Collections.singleton(qry));
		return qry.getOutput();
	}
}
