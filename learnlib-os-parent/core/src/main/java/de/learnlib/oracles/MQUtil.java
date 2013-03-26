package de.learnlib.oracles;

import java.util.Collections;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;

public abstract class MQUtil {
	
	public static <I,O> O query(MembershipOracle<I,O> oracle, Word<I> queryWord) {
		DefaultQuery<I,O> qry = new DefaultQuery<>(queryWord);
		oracle.processQueries(Collections.singleton(qry));
		return qry.getOutput();
	}
	
	public static <I,O> O query(MembershipOracle<I,O> oracle, Word<I> prefix, Word<I> suffix) {
		DefaultQuery<I,O> qry = new DefaultQuery<>(prefix, suffix);
		oracle.processQueries(Collections.singleton(qry));
		return qry.getOutput();
	}
}
