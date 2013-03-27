package de.learnlib.algorithms.lstargeneric.components.closing;

import de.learnlib.algorithms.lstargeneric.closing.CloseFirstStrategy;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "CloseFirstStrategy", type = ClosingStrategy.class)
public class CloseFirstStrategyFactory<I, O> implements
		LLComponentFactory<CloseFirstStrategy<I, O>> {

	@Override
	public CloseFirstStrategy<I, O> instantiate() {
		return CloseFirstStrategy.getInstance();
	}

}
