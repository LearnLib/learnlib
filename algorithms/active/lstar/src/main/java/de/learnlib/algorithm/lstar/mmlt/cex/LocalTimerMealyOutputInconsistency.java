package de.learnlib.algorithm.lstar.mmlt.cex;


import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealyOutputSymbol;
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
public record LocalTimerMealyOutputInconsistency<I, O>(Word<LocalTimerMealySemanticInputSymbol<I>> prefix,
                                                       Word<LocalTimerMealySemanticInputSymbol<I>> suffix,
                                                       Word<LocalTimerMealyOutputSymbol<O>> targetOut,
                                                       Word<LocalTimerMealyOutputSymbol<O>> hypOut) {
}
