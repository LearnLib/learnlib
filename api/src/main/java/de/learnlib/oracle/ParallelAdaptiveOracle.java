package de.learnlib.oracle;

import de.learnlib.oracle.AdaptiveMembershipOracle;


public interface ParallelAdaptiveOracle<I, O> extends ThreadPool, AdaptiveMembershipOracle<I, O> {}
