package de.learnlib.algorithms.dhc.mealy.factory;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.algorithms.dhc.mealy.MealyDHC;
import de.learnlib.factory.LearnerBuilder;

public class MealyDHCBuilder<I, O> extends LearnerBuilder<MealyMachine<?,I,?,O>, I, Word<O>, MealyDHCBuilder<I,O>> {

	@Override
	protected MealyDHCBuilder<I, O> _this() {
		return this;
	}

	@Override
	public MealyDHC<I, O> create() {
		return new MealyDHC<>(alphabet, oracle);
	}

}
