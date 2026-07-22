/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithm.lstar.mmlt.cex;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithm.lstar.mmlt.MMLTHypothesis;
import de.learnlib.oracle.TimedQueryOracle;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the search for an extended decomposition of a truncated counterexample and the post-processing of an
 * extended decomposition.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
class MMLTCounterexampleDecomposer<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMLTCounterexampleDecomposer.class);

    private final TimedQueryOracle<I, O> timeOracle;
    private final AcexAnalyzer acexAnalyzer;

    MMLTCounterexampleDecomposer(TimedQueryOracle<I, O> timeOracle, AcexAnalyzer acexAnalyzer) {
        this.timeOracle = timeOracle;
        this.acexAnalyzer = acexAnalyzer;
    }

    ExtendedDecomposition<I, O> findExtendedDecomposition(MMLTOutputInconsistency<I, O> outIncons,
                                                          MMLTHypothesis<I, O> hypothesis) {

        if (outIncons.suffix().length() == 1) {
            // Incorrect output:
            State<Integer, O> prefixState = hypothesis.getSemantics().getState(outIncons.prefix());
            assert prefixState != null;
            return new ExtendedDecomposition<>(prefixState, outIncons.suffix().firstSymbol());
        }

        // Verify breakpoint condition:
        MMLTInconsPrefixTransformAcex<I, O> acex = new MMLTInconsPrefixTransformAcex<>(outIncons.suffix(),
                                                                                       timeOracle,
                                                                                       w -> hypothesis.getPrefix(
                                                                                               outIncons.prefix()
                                                                                                        .concat(w)));

        if (acex.testEffects(0, acex.getLength() - 1)) {
            // Breakpoint condition not met -> must be incorrect output:
            Word<TimedInput<I>> lastStatePrefix = outIncons.prefix().concat(outIncons.suffix().prefix(-1));
            State<Integer, O> lastState = hypothesis.getSemantics().getState(lastStatePrefix);
            assert lastState != null;
            return new ExtendedDecomposition<>(lastState, outIncons.suffix().lastSymbol());
        }

        // Breakpoint condition met -> find decomposition:
        int breakpoint = this.acexAnalyzer.analyzeAbstractCounterexample(acex);

        assert !acex.testEffects(breakpoint, breakpoint + 1) : "Failed to find valid decomposition.";

        // Get components:
        Word<TimedInput<I>> prefix = outIncons.prefix().concat(outIncons.suffix().prefix(breakpoint));
        TimedInput<I> sym = outIncons.suffix().getSymbol(breakpoint);
        Word<TimedInput<I>> discriminator = outIncons.suffix().subWord(breakpoint + 1);

        State<Integer, O> prefixState = hypothesis.getSemantics().getState(prefix);
        assert prefixState != null;

        LOGGER.debug("""
                             Decomposing to {}|{}|{}
                             Output at {}: {}.
                             Output at {}: {}
                             """,
                     prefixState,
                     sym,
                     discriminator,
                     breakpoint,
                     acex.computeEffect(breakpoint),
                     breakpoint + 1,
                     acex.computeEffect(breakpoint + 1));

        return new ExtendedDecomposition<>(prefixState, sym, discriminator);
    }

    /**
     * Post-processes an extended decomposition: if the decomposition corresponds to a transition with an incorrect
     * target or output at a timeout symbol, transforms the decomposition so that the input is either a non-delaying
     * input or a single time step.
     *
     * @param decomposition
     *         the extended decomposition
     *
     * @return the post-processed decomposition
     */
    ExtendedDecomposition<I, O> postProcessExtendedDecomposition(ExtendedDecomposition<I, O> decomposition,
                                                                 MMLTHypothesis<I, O> hypothesis) {
        if (!(decomposition.input() instanceof TimeoutSymbol<I>)) {
            return decomposition;
        }

        Word<TimedInput<I>> statePrefix = hypothesis.getPrefix(decomposition.state());
        Word<TimedOutput<O>> hypOutput =
                hypothesis.computeSuffixOutput(statePrefix, Word.fromLetter(decomposition.input()));
        Word<TimedOutput<O>> sulOutput = timeOracle.answerQuery(statePrefix, Word.fromLetter(decomposition.input()));

        if (decomposition.isForIncorrectOutput()) {
            assert hypOutput.firstSymbol().delay() != 0 || sulOutput.firstSymbol().delay() == 0;

            // Incorrect output at tout:
            long minWaitTime;
            if (hypOutput.firstSymbol().delay() != 0 && sulOutput.firstSymbol().delay() == 0) {
                // If there is no timeout in either hyp or sul, need to trigger next observable timeout:
                minWaitTime = hypOutput.firstSymbol().delay();
            } else {
                // If there is a timeout in hyp and sul, go to next timeout:
                minWaitTime = Math.min(hypOutput.firstSymbol().delay(), sulOutput.firstSymbol().delay());
            }

            // if minimum time is zero (= no timeout) or one, need to append empty word to prefix:
            State<Integer, O> newPrefixState;
            if (minWaitTime <= 1) {
                newPrefixState = decomposition.state();
            } else {
                newPrefixState =
                        hypothesis.getSemantics().getState(statePrefix.append(TimedInput.step(minWaitTime - 1)));
                assert newPrefixState != null;
            }

            LOGGER.debug("Updated incorrect output at tout during post-processing.");
            return new ExtendedDecomposition<>(newPrefixState, TimedInput.step());
        } else {
            if (decomposition.state().isStableConfig() || hypOutput.equals(sulOutput)) {
                // stable-configuration or same output at tout -> same wait time for output in hyp and SUL:
                assert hypOutput.firstSymbol().delay() == sulOutput.firstSymbol().delay();

                long waitTime = hypOutput.firstSymbol().delay();
                State<Integer, O> newPrefixState;
                if (waitTime <= 1) {
                    newPrefixState = decomposition.state();
                } else {
                    newPrefixState =
                            hypothesis.getSemantics().getState(statePrefix.append(TimedInput.step(waitTime - 1)));
                    assert newPrefixState != null;
                }

                LOGGER.debug("Updated incorrect target at tout during post-processing.");
                return new ExtendedDecomposition<>(newPrefixState, TimedInput.step(), decomposition.discriminator());
            } else {
                // different output at tout -> found incorrect output:
                LOGGER.debug("Found incorrect output through post-processing.");
                return postProcessExtendedDecomposition(new ExtendedDecomposition<>(decomposition.state(),
                                                                                    decomposition.input()), hypothesis);
            }
        }
    }
}
