package de.learnlib.algorithms.lstargeneric.components.ce;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ShahbazCEXHandler;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "ShahbazCEXHandler", type = ObservationTableCEXHandler.class)
public class ShahbazCEXHandlerFactory<I, O> implements
		LLComponentFactory<ShahbazCEXHandler<I, O>> {

	@Override
	public ShahbazCEXHandler<I, O> instantiate() {
		return ShahbazCEXHandler.getInstance();
	}

}
