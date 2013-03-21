package de.learnlib.algorithms.angluin.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.FastAlphabet;
import net.automatalib.words.impl.Symbol;

import java.util.Collection;

public class SimpleOracle implements MembershipOracle<Symbol, Boolean> {

	private final static Symbol zero = new Symbol(0);
	private final static Symbol one = new Symbol(1);

	public static Alphabet<Symbol> getAlphabet() {
		Alphabet<Symbol> alphabet = new FastAlphabet<>();
		alphabet.add(zero);
		alphabet.add(one);
		return alphabet;
	}

	@Override
	public void processQueries(Collection<Query<Symbol, Boolean>> queries) {
		for (Query<Symbol, Boolean> query : queries) {
			query.setOutput(determineOutput(query.getInput()));
		}
	}

	private Boolean determineOutput(Word<Symbol> input) {
		if (input.size() == 3) {
			return false;
		}

		for (Symbol symbol : input) {
			if (symbol.equals(one)) {
				return true;
			}
		}

		return false;
	}

}
