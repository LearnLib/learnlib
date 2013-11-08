package de.learnlib.algorithms.baselinelstar.factory;

import net.automatalib.automata.fsa.DFA;
import de.learnlib.algorithms.baselinelstar.BaselineLStar;
import de.learnlib.factory.LearnerBuilder;

public class BaselineLStarBuilder<I> extends LearnerBuilder<DFA<?,I>, I, Boolean, BaselineLStarBuilder<I>> {

	@Override
	protected BaselineLStarBuilder<I> _this() {
		return this;
	}

	@Override
	public BaselineLStar<I> create() {
		return new BaselineLStar<>(alphabet, oracle);
	}


}
