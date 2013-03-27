package de.learnlib.algorithms.lstargeneric.components.ce;

import de.learnlib.algorithms.lstargeneric.ce.ClassicLStarCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.components.LLComponent;
import de.learnlib.components.LLComponentFactory;

@LLComponent(name = "ClassicLStarCEXHandler", type = ObservationTableCEXHandler.class)
public class ClassicLStarCEXHandlerFactory<I, O> implements
		LLComponentFactory<ClassicLStarCEXHandler<I, O>> {

	@Override
	public ClassicLStarCEXHandler<I, O> instantiate() {
		return ClassicLStarCEXHandler.getInstance();
	}

}
