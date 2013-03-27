package de.learnlib.algorithms.lstargeneric.components.closing;

import de.learnlib.algorithms.lstargeneric.closing.CloseLexMinStrategy;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "CloseLexMinStrategy", type = ClosingStrategy.class)
public class CloseLexMinStrategyFactory<I, O> implements
		LLComponentFactory<CloseLexMinStrategy<I, O>> {

	@Override
	public CloseLexMinStrategy<I, O> instantiate() {
		return CloseLexMinStrategy.getInstance();
	}

}
