package de.learnlib.mappers;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;

public abstract class AbstractContextExecutableInputSUL<I extends ContextExecutableInput<? extends O,? super C>, O, C> implements SUL<I,O> {
	
	protected abstract C createContext();
	protected abstract void disposeContext(C context);
	
	private C currentContext;
	
	@Override
	public void pre() {
		this.currentContext = createContext();
	}
	
	@Override
	public void post() {
		disposeContext(currentContext);
		currentContext = null;
	}
	
	@Override
	public O step(I in) throws SULException {
		try {
			return in.execute(currentContext);
		}
		catch(SULException ex) {
			throw ex;
		}
		catch(Exception ex) {
			throw new SULException(ex);
		}
	}
}
