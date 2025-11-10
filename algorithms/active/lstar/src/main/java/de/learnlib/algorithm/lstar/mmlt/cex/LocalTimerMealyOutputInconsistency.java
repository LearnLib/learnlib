package de.learnlib.algorithm.lstar.mmlt.cex;


import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;

/**
 * Represents an output inconsistency used by the MMLT learner.
 *
 * @param prefix    Prefix
 * @param suffix    Suffix input
 * @param targetOut Suffix output in SUL
 * @param hypOut    Suffix output in hypothesis
 * @param <I>       Input type for non-delaying inputs
 * @param <O>       Output symbol type
 */
public record LocalTimerMealyOutputInconsistency<I, O>(Word<TimedInput<I>> prefix,
                                                       Word<TimedInput<I>> suffix,
                                                       Word<TimedOutput<O>> targetOut,
                                                       Word<TimedOutput<O>> hypOut) {
}
