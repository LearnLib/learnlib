package de.learnlib.algorithms.lstargeneric.components.closing;

import de.learnlib.algorithms.lstargeneric.closing.CloseShortestStrategy;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "CloseShortestStrategy", type = ClosingStrategy.class)
public class CloseShortestStrategyFactory<I, O> implements
		LLComponentFactory<CloseShortestStrategy<I, O>> {

	@Override
	public CloseShortestStrategy<I, O> instantiate() {
		return CloseShortestStrategy.getInstance();
	}

}
