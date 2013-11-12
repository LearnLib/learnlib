package de.learnlib.algorithms.lstargeneric.dfa.factory;

import net.automatalib.automata.fsa.DFA;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.algorithms.lstargeneric.factory.ExtensibleAutomatonLStarBuilder;

public class ExtensibleLStarDFABuilder<I> extends
		ExtensibleAutomatonLStarBuilder<DFA<?,I>, I, Boolean, ExtensibleLStarDFABuilder<I>> {

	@Override
	protected ExtensibleLStarDFABuilder<I> _this() {
		return this;
	}

	@Override
	public ExtensibleLStarDFA<I> create() {
		return new ExtensibleLStarDFA<>(alphabet, oracle, initialSuffixes, cexHandler, closingStrategy);
	}

}
