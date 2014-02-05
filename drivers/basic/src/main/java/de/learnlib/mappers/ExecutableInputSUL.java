package de.learnlib.mappers;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import de.learnlib.drivers.api.ExecutableInput;

public class ExecutableInputSUL<I extends ExecutableInput<? extends O>, O> implements SUL<I, O> {

	@Override
	public void pre() {
	}

	@Override
	public void post() {
	}

	@Override
	public O step(I in) throws SULException {
		try {
			return in.execute();
		}
		catch(SULException ex) {
			throw ex;
		}
		catch(Exception ex) {
			throw new SULException(ex);
		}
	}
}
