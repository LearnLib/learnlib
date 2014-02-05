package de.learnlib.mappers;

import de.learnlib.api.SULException;



public interface ContextExecutableInput<O, C> {
	public O execute(C context) throws SULException, Exception;
}
