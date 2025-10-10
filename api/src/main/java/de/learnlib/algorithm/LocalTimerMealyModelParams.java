package de.learnlib.algorithm;

import net.automatalib.automaton.time.mmlt.AbstractSymbolCombiner;

/**
 * Model-specific parameters for the MMLT-learner.
 * These are used by various filters, oracles, and the MMLT simulator.
 *
 * @param silentOutput             Silent output symbol
 * @param maxTimeoutWaitingTime    Maximum waiting time for a timeout symbol
 * @param maxTimerQueryWaitingTime Maximum waiting time for timer queries
 * @param outputCombiner           Function for combining simultaneously occurring outputs of timers
 * @param <O>                      Output symbol type
 */
public record LocalTimerMealyModelParams<O>(O silentOutput,
                                            long maxTimeoutWaitingTime,
                                            long maxTimerQueryWaitingTime,
                                            AbstractSymbolCombiner<O> outputCombiner) {
}
