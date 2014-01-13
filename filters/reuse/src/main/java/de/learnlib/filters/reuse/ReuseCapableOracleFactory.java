package de.learnlib.filters.reuse;

/**
 * A factory which is able to create {@link de.learnlib.filters.reuse.ReuseCapableOracle}s
 * to be used by a {@link de.learnlib.filters.reuse.ReuseOracle} in a thread-safe manner.
 *
 * @param <S>
 * 		The system state used by the reuse oracle
 * @param <I>
 * 		The input type
 * @param <O>
 * 		The output type
 */
public interface ReuseCapableOracleFactory<S,I,O> {

	public ReuseCapableOracle<S,I,O> createOracle();

}
