package de.learnlib.algorithm.lstar.mmlt.cex;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithm.lstar.mmlt.hyp.LocalTimerMealyHypothesis;
import de.learnlib.oracle.TimedQueryOracle;
import net.automatalib.alphabet.time.mmlt.LocalTimerMealySemanticInputSymbol;
import net.automatalib.alphabet.time.mmlt.TimeStepSequence;
import net.automatalib.alphabet.time.mmlt.TimeStepSymbol;
import net.automatalib.alphabet.time.mmlt.TimeoutSymbol;
import net.automatalib.automaton.time.mmlt.semantics.LocalTimerMealyConfiguration;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the search for an extended decomposition of a truncated counterexample and the post-processing of an extended decomposition.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
class LocalTimerMealyCounterexampleDecompositor<S, I, O> {

    private static final Logger logger = LoggerFactory.getLogger(LocalTimerMealyCounterexampleDecompositor.class);


    private final TimedQueryOracle<I, O> timeOracle;
    private final AcexAnalyzer acexAnalyzer;

    public LocalTimerMealyCounterexampleDecompositor(TimedQueryOracle<I, O> timeOracle, AcexAnalyzer acexAnalyzer) {
        this.timeOracle = timeOracle;
        this.acexAnalyzer = acexAnalyzer;
    }

    ExtendedDecomposition<S, I, O> findExtendedDecomposition(LocalTimerMealyOutputInconsistency<I, O> outIncons,
                                                             LocalTimerMealyHypothesis<S, I, O> hypothesis) {

        if (outIncons.suffix().length() == 1) {
            // Incorrect output:
            var prefixState = hypothesis.getSemantics().traceInputs(outIncons.prefix());
            return new ExtendedDecomposition<>(prefixState, outIncons.suffix().firstSymbol());
        }

        // Verify breakpoint condition:
        LocalTimerMealyInconsPrefixTransformAcex<I, O> acex = new LocalTimerMealyInconsPrefixTransformAcex<>(outIncons.suffix(), timeOracle,
                w -> hypothesis.getPrefix(outIncons.prefix().concat(w)));

        if (acex.testEffects(0, acex.getLength() - 1)) {
            // Breakpoint condition not met -> must be incorrect output:
            var lastStatePrefix = outIncons.prefix().concat(outIncons.suffix().prefix(outIncons.suffix().length() - 1));
            var lastState = hypothesis.getSemantics().traceInputs(lastStatePrefix);

            return new ExtendedDecomposition<>(lastState, outIncons.suffix().lastSymbol());
        }

        // Breakpoint condition met -> find decomposition:
        int breakpoint = this.acexAnalyzer.analyzeAbstractCounterexample(acex);
        if (acex.testEffects(breakpoint, breakpoint + 1)) {
            throw new AssertionError("Failed to find valid decomposition.");
        }

        // Get components:
        Word<LocalTimerMealySemanticInputSymbol<I>> prefix = outIncons.prefix().concat(outIncons.suffix().prefix(breakpoint));
        LocalTimerMealySemanticInputSymbol<I> sym = outIncons.suffix().getSymbol(breakpoint);
        Word<LocalTimerMealySemanticInputSymbol<I>> discriminator = outIncons.suffix().subWord(breakpoint + 1);

        var prefixState = hypothesis.getSemantics().traceInputs(prefix);

        logger.debug(String.format("Decomposing to %s|%s|%s %n" + "Output at %d: %s. %nOutput at %d: %s", prefixState, sym, discriminator, breakpoint,
                acex.computeEffect(breakpoint), breakpoint + 1, acex.computeEffect(breakpoint + 1)));

        return new ExtendedDecomposition<>(prefixState, sym, discriminator);
    }

    /**
     * Post-processes an extended decomposition: if the decomposition corresponds to a transition with an incorrect target or output at a timeout symbol,
     * transforms the decomposition so that the input is either a non-delaying input or a single time step.
     *
     * @param decomposition Extended decomposition
     * @return Post-processed decomposition
     */
    ExtendedDecomposition<S, I, O> postProcessExtendedDecomposition(ExtendedDecomposition<S, I, O> decomposition,
                                                                    LocalTimerMealyHypothesis<S, I, O> hypothesis) {
        if (!(decomposition.input() instanceof TimeoutSymbol<I>)) {
            return decomposition;
        }

        var statePrefix = hypothesis.getPrefix(decomposition.state());
        var hypOutput = hypothesis.getSemantics().computeSuffixOutput(statePrefix, Word.fromLetter(decomposition.input()));
        var sulOutput = timeOracle.querySuffixOutput(statePrefix, Word.fromLetter(decomposition.input()));

        if (decomposition.isForIncorrectOutput()) {
            // Incorrect output at tout:
            long minWaitTime;
            if (hypOutput.firstSymbol().getDelay() == 0 && sulOutput.firstSymbol().getDelay() != 0) {
                throw new AssertionError();
            } else if (hypOutput.firstSymbol().getDelay() != 0 && sulOutput.firstSymbol().getDelay() == 0) {
                // If there is no timeout in either hyp or sul, need to trigger next observable timeout:
                minWaitTime = hypOutput.firstSymbol().getDelay();
            } else {
                // If there is a timeout in hyp and sul, go to next timeout:
                minWaitTime = Math.min(hypOutput.firstSymbol().getDelay(), sulOutput.firstSymbol().getDelay());
            }

            // if minimum time is zero (= no timeout) or one, need to append empty word to prefix:
            LocalTimerMealyConfiguration<S, I, O> newPrefixState;
            if (minWaitTime <= 1) {
                newPrefixState = decomposition.state();
            } else {
                newPrefixState = hypothesis.getSemantics().traceInputs(statePrefix.append(new TimeStepSequence<>(minWaitTime - 1)));
            }

            logger.debug("Updated incorrect output at tout during post-processing.");
            return new ExtendedDecomposition<>(newPrefixState, new TimeStepSymbol<>());
        } else {
            if (decomposition.state().isStableConfig() || hypOutput.equals(sulOutput)) {
                // stable-configuration or same output at tout -> same wait time for output in hyp and SUL:
                if (hypOutput.firstSymbol().getDelay() != sulOutput.firstSymbol().getDelay()) {
                    throw new AssertionError();
                }

                long waitTime = hypOutput.firstSymbol().getDelay();
                LocalTimerMealyConfiguration<S, I, O> newPrefixState;
                if (waitTime <= 1) {
                    newPrefixState = decomposition.state();
                } else {
                    newPrefixState = hypothesis.getSemantics().traceInputs(statePrefix.append(new TimeStepSequence<>(waitTime - 1)));
                }

                logger.debug("Updated incorrect target at tout during post-processing.");
                return new ExtendedDecomposition<>(newPrefixState, new TimeStepSymbol<>(), decomposition.discriminator());
            } else {
                // different output at tout -> found incorrect output:
                logger.debug("Found incorrect output through post-processing.");
                return postProcessExtendedDecomposition(new ExtendedDecomposition<>(decomposition.state(), decomposition.input()), hypothesis);
            }
        }
    }
}
