package de.learnlib.algorithms.lstargeneric.components.ce;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.Suffix1by1CEXHandler;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "Suffix1by1CEXHandler", type = ObservationTableCEXHandler.class)
public class Suffix1by1CEXHandlerFactory<I, O> implements
		LLComponentFactory<Suffix1by1CEXHandler<I, O>> {

	@Override
	public Suffix1by1CEXHandler<I, O> instantiate() {
		return Suffix1by1CEXHandler.getInstance();
	}

}
