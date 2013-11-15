package de.learnlib.filters.reuse.symbols;

import java.util.HashMap;
import java.util.Map;

import de.learnlib.drivers.api.SULException;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.api.SystemStateRef;

public class KeyValueResetSymbol
		implements
		InjectableSystemStateRef<SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String>, KeyValueSymbol, String> {

	private SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String> ssr;

	@Override
	public String execute() throws SULException {
		return "not necessary";
	}

	@Override
	public void inject(SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String> ssr) {
		this.ssr = ssr;
	}

	@Override
	public SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String> retrieve() {
		this.ssr = new SystemStateRef<>();
		ssr.setSystemState(new HashMap<Integer, Object>());
		return this.ssr;
	}
}
