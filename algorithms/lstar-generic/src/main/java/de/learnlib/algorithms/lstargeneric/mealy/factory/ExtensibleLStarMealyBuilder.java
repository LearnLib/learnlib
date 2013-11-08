package de.learnlib.algorithms.lstargeneric.mealy.factory;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.algorithms.lstargeneric.factory.ExtensibleAutomatonLStarBuilder;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;

public class ExtensibleLStarMealyBuilder<I, O> extends
		ExtensibleAutomatonLStarBuilder<MealyMachine<?, I, ?, O>, I, Word<O>,ExtensibleLStarMealyBuilder<I,O>> {

	@Override
	public ExtensibleLStarMealy<I, O> create() {
		return new ExtensibleLStarMealy<>(alphabet, oracle, initialSuffixes, cexHandler, closingStrategy);
	}

	@Override
	protected ExtensibleLStarMealyBuilder<I, O> _this() {
		return this;
	}


}
