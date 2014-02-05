package de.learnlib.mappers;

public class ContextExecutableInputSUL<I extends ContextExecutableInput<? extends O, ? super C>, O, C>
		extends AbstractContextExecutableInputSUL<I, O, C> {
	
	public static interface ContextHandler<C> {
		public C createContext();
		public void disposeContext(C context);
	}
	
	private final ContextHandler<C> contextHandler;
	
	public ContextExecutableInputSUL(ContextHandler<C> contextHandler) {
		this.contextHandler = contextHandler;
	}

	@Override
	protected C createContext() {
		return contextHandler.createContext();
	}

	@Override
	protected void disposeContext(C context) {
		contextHandler.disposeContext(context);
	}
	
}
