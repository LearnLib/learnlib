package de.learnlib.algorithms.angluin.oracles;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.ls5.words.Word;
import de.ls5.words.impl.Symbol;

import java.util.List;

public class SimpleOracle implements MembershipOracle<Symbol, Boolean> {

	@Override
	public void processQueries(List<Query<Symbol, Boolean>> queries) {
		for (Query<Symbol, Boolean> query : queries) {
			query.setOutput(determineOutput(query.getInput()));
		}
	}

	private Boolean determineOutput(Word<Symbol> input) {
		if (input.size() == 3) {
			return true;
		}

		for (Symbol symbol : input) {
			if (symbol.getUserObject().equals(1)) {
				return true;
			}
		}

		return false;
	}

}
