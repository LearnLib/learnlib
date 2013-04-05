package de.learnlib.filters.reuse;

import de.learnlib.filters.reuse.api.ExecutableSymbol;
import de.learnlib.filters.reuse.api.SystemState;
import de.learnlib.logging.LearnLogger;

class MySymbol implements ExecutableSymbol<String> {
	private int VAL;
	private ReuseOracleTest reuseOracleTest;

	public MySymbol(ReuseOracleTest reuseOracleTest, int value) {
		this.reuseOracleTest = reuseOracleTest;
		this.VAL = value;
	}

	@Override
	public String execute(SystemState state) {
		int value;
		if (state.get("VALUE") != null) {
			value = (Integer) state.get("VALUE");
		}
		else {
			value = 0;
		}

		LearnLogger.getLogger(MySymbol.class.getName()).info("value " + value + ", add " + VAL + "?");

		if (VAL - 1 == value) {
			value += VAL;
			state.put("VALUE", value);

			LearnLogger.getLogger(MySymbol.class.getName()).info("value " + value);

			return "ACK";
		}
		else {
			LearnLogger.getLogger(MySymbol.class.getName()).info("value " + value);
			return "NAK";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getOuterType().hashCode();
		result = prime * result + VAL;
		return result;
	}


	@Override
	public String toString() {
		return "[" + VAL + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MySymbol other = (MySymbol) obj;
		if (!getOuterType().equals(other.getOuterType())) {
			return false;
		}
		if (VAL != other.VAL) {
			return false;
		}
		return true;
	}

	private ReuseOracleTest getOuterType() {
		return reuseOracleTest;
	}
}
