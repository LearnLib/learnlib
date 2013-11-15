package de.learnlib.filters.reuse.symbols;

import java.util.Map;

import de.learnlib.drivers.api.SULException;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.api.SystemStateRef;

public class KeyValueSymbol 
	implements
	InjectableSystemStateRef<SystemStateRef<Map<Integer,Object>,KeyValueSymbol, String>, KeyValueSymbol, String> {

	private SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String> ssr;
	
	private Integer pos;
	private boolean clear;
	
	public KeyValueSymbol(Integer pos, boolean clear) {
		this.pos = pos;
		this.clear = clear;
	}
	
	@Override
	public String execute() throws SULException {
		Map<Integer, Object> map = ssr.getSystemState();
		if (clear) {
			if (map.get(pos) == null) {
				return "alreadycleared";
			} else {
				map.put(pos, null);
				return "cleared";
			}
			
		} else {
			if (map.get(pos) == null) {
				map.put(pos, "some object");
				return "putted";
			} else {
				return "alreadyput";
			}
		}
	}
	
	@Override
	public String toString() {
		if (clear) {
			return "[" + pos + ", clear]";
		} else {
			return "[" + pos + ", put]";
		}
	}

	@Override
	public void inject(
			SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String> ssr) {
		this.ssr = ssr;
	}

	@Override
	public SystemStateRef<Map<Integer, Object>, KeyValueSymbol, String> retrieve() {
		return this.ssr;
	}
}
