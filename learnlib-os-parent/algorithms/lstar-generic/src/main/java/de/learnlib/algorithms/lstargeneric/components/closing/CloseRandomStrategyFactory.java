package de.learnlib.algorithms.lstargeneric.components.closing;

import de.learnlib.algorithms.lstargeneric.closing.CloseRandomStrategy;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "CloseRandomStrategy", type = ClosingStrategy.class)
public class CloseRandomStrategyFactory<I, O> implements
		LLComponentFactory<CloseRandomStrategy<I, O>> {

	@Override
	public CloseRandomStrategy<I, O> instantiate() {
		return CloseRandomStrategy.getInstance();
	}

}
