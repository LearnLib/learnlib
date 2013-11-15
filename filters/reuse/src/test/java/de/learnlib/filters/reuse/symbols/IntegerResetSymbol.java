package de.learnlib.filters.reuse.symbols;

import de.learnlib.drivers.api.SULException;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.ssrs.IntegerSystemStateRef;

public class IntegerResetSymbol implements InjectableSystemStateRef<IntegerSystemStateRef<IntegerSymbol, String>, IntegerSymbol, String>{
	private IntegerSystemStateRef<IntegerSymbol, String> ssr;

	@Override
	public String execute() throws SULException {
		return "reset_done";
	}

	@Override
	public IntegerSystemStateRef<IntegerSymbol, String> retrieve() {
		ssr = new IntegerSystemStateRef<>();
		ssr.setSystemState(0);
		return ssr;
	}

	@Override
	public void inject(IntegerSystemStateRef<IntegerSymbol, String> ssr) {
		this.ssr = ssr;
	}
}
