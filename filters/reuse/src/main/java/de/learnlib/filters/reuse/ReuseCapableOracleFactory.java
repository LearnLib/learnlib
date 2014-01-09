package de.learnlib.filters.reuse;

public interface ReuseCapableOracleFactory<S,I,O> {

	public ReuseCapableOracle<S,I,O> createOracle();

}
