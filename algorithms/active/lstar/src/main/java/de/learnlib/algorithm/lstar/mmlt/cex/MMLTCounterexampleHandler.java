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

import java.util.List;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithm.lstar.mmlt.ExtensibleLStarMMLT;
import de.learnlib.algorithm.lstar.mmlt.MMLTHypothesis;
import de.learnlib.algorithm.lstar.mmlt.cex.results.CexAnalysisResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.FalseIgnoreResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingDiscriminatorResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingOneShotResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingResetResult;
import de.learnlib.filter.FilterResponse;
import de.learnlib.filter.SymbolFilter;
import de.learnlib.oracle.TimedQueryOracle;
import net.automatalib.automaton.impl.CompactTransition;
import net.automatalib.automaton.mmlt.MMLT;
import net.automatalib.automaton.mmlt.State;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes a truncated counterexample for a hypothesis {@link MMLT} by searching for an extended decomposition,
 * post-processing it, and inferring an inaccuracy from it.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class MMLTCounterexampleHandler<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MMLTCounterexampleHandler.class);

    protected final TimedQueryOracle<I, O> timeOracle;
    private final MMLTCounterexampleDecomposer<I, O> decomposer;
    private final SymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;

    public MMLTCounterexampleHandler(TimedQueryOracle<I, O> timeOracle,
                                     AcexAnalyzer acexAnalyzer,
                                     SymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter) {
        this.timeOracle = timeOracle;
        this.decomposer = new MMLTCounterexampleDecomposer<>(timeOracle, acexAnalyzer);
        this.symbolFilter = symbolFilter;
    }

    public CexAnalysisResult<I, O> analyzeInconsistency(MMLTOutputInconsistency<I, O> outIncons,
                                                        MMLTHypothesis<I, O> hypothesis) {

        // Search for an extended decomposition:
        ExtendedDecomposition<I, O> decomposition = decomposer.findExtendedDecomposition(outIncons, hypothesis);
        LOGGER.debug("Found an extended decomposition: {}", decomposition);

        // Post-process the decomposition:
        decomposition = decomposer.postProcessExtendedDecomposition(decomposition, hypothesis);
        LOGGER.debug("Post-processed decomposition: {}", decomposition);

        if (decomposition.isForIncorrectOutput()) {
            return handleIncorrectOutput(decomposition, hypothesis);
        } else {
            return handleIncorrectTarget(decomposition, hypothesis);
        }
    }

    private CexAnalysisResult<I, O> handleIncorrectOutput(ExtendedDecomposition<I, O> decomposition,
                                                          MMLTHypothesis<I, O> hypothesis) {
        // Transition with incorrect output always implies missing one-shot timer:
        LOGGER.debug("Found missing one-shot via incorrect output.");
        return this.selectOneShotTimer(decomposition, hypothesis, decomposition.state().getEntryDistance());
    }

    private CexAnalysisResult<I, O> handleIncorrectTarget(ExtendedDecomposition<I, O> decomposition,
                                                          MMLTHypothesis<I, O> hypothesis) {
        if (decomposition.input() instanceof InputSymbol<I> ndi) {
            // If decomposition at non-delaying input + considered as self-loop, treat as false ignore:
            if (symbolFilter.query(hypothesis.getLocationPrefix(decomposition.state()), ndi) == FilterResponse.IGNORE) {
                return new FalseIgnoreResult<>(decomposition.state().getLocation(), ndi);
            }

            return this.handleIncorrectTargetNonDelaying(decomposition, hypothesis);

        } else if (decomposition.input() instanceof TimeStepSequence<I>) {
            return this.handleIncorrectTargetTimeStep(decomposition, hypothesis);
        } else {
            throw new AssertionError("Unexpected symbol type.");
        }
    }

    private CexAnalysisResult<I, O> selectOneShotTimer(ExtendedDecomposition<I, O> decomposition,
                                                       MMLTHypothesis<I, O> hypothesis,
                                                       long maxInitialValue) {
        List<TimerInfo<Integer, O>> timers = hypothesis.getSortedTimers(decomposition.state().getLocation());
        int newOneShot = ExtensibleLStarMMLT.selectOneShotTimer(timers, maxInitialValue);
        TimerInfo<Integer, O> timer = timers.get(newOneShot);
        LOGGER.debug("Missing one-shot: setting ({}|{}) to one-shot.",
                     hypothesis.getLocationPrefix(decomposition.state()),
                     timer);
        return new MissingOneShotResult<>(decomposition.state().getLocation(), timer);
    }

    private CexAnalysisResult<I, O> handleIncorrectTargetTimeStep(ExtendedDecomposition<I, O> decomposition,
                                                                  MMLTHypothesis<I, O> hypothesis) {
        // Check if there is a one-shot timer expiring at the next time step:
        List<? extends TimerInfo<?, O>> localTimers = hypothesis.getSortedTimers(decomposition.state().getLocation());
        assert !localTimers.isEmpty();

        // If location has a one-shot timer, this is the one with the highest initial value:
        TimerInfo<?, O> lastTimer = localTimers.get(localTimers.size() - 1);
        if (!lastTimer.periodic()) {
            assert lastTimer.initial() - 1 == decomposition.state().getEntryDistance() :
                    "Incorrect target must be at timeout of non-periodic timer.";
            Word<TimedInput<I>> discriminator = decomposition.discriminator();
            assert discriminator != null;
            LOGGER.debug("Inferred missing discriminator at timeout.");
            return new MissingDiscriminatorResult<>(decomposition.state().getLocation(),
                                                    decomposition.input(),
                                                    discriminator);
        } else if (!decomposition.state().isStableConfig()) {
            LOGGER.debug("Found missing one-shot via incorrect target in non-stable config.");
            return this.selectOneShotTimer(decomposition, hypothesis, decomposition.state().getEntryDistance());
        } else {
            LOGGER.debug("Found missing one-shot via incorrect target in stable config.");
            return new MissingOneShotResult<>(decomposition.state().getLocation(),
                                              localTimers.get(0)); // lowest initial value
        }
    }

    private CexAnalysisResult<I, O> handleIncorrectTargetNonDelaying(ExtendedDecomposition<I, O> decomposition,
                                                                     MMLTHypothesis<I, O> hypothesis) {
        // 1: can be a missing discriminator?

        // Check if correct target in entry w.r.t. discriminator:
        Word<TimedInput<I>> transPrefix =
                hypothesis.getLocationPrefix(decomposition.state()).append(decomposition.input());
        State<Integer, O> succState = hypothesis.getSemantics().getState(transPrefix); // successor state in hypothesis
        Word<TimedInput<I>> discriminator = decomposition.discriminator();

        assert succState != null && discriminator != null;

        Word<TimedOutput<O>> actualSuffixOutput = this.timeOracle.answerQuery(transPrefix, discriminator);
        Word<TimedOutput<O>> expSuffixOutput =
                this.timeOracle.answerQuery(hypothesis.getPrefix(succState), discriminator);

        if (!actualSuffixOutput.equals(expSuffixOutput)) {
            LOGGER.debug("Inferred missing discriminator at non-delaying input.");
            return new MissingDiscriminatorResult<>(decomposition.state().getLocation(),
                                                    decomposition.input(),
                                                    discriminator);
        }

        // 2: can be a local reset?
        if (decomposition.state().isStableConfig()) {
            LOGGER.debug("Inferred missing reset in stable config.");
            return new MissingResetResult<>(decomposition.state().getLocation(),
                                            (InputSymbol<I>) decomposition.input());
        }

        // Non-stable -> explicitly test for missing reset:
        boolean isLocalReset = hypothesis.isLocalReset(decomposition.state().getLocation(),
                                                       ((InputSymbol<I>) decomposition.input()).symbol());
        CompactTransition<O> trans = hypothesis.getTransition(decomposition.state().getLocation(),
                                                              ((InputSymbol<I>) decomposition.input()).symbol());
        assert trans != null;
        Integer successor = hypothesis.getSuccessor(trans);

        // Must loop without reset:
        if (successor.equals(decomposition.state().getLocation()) && !isLocalReset) {
            // Must have at least two stable configs:
            TimerInfo<Integer, O> firstTimer = hypothesis.getSortedTimers(decomposition.state().getLocation()).get(0);
            if (firstTimer.initial() > 1) {
                // Must not self-loop in at least one non-entry stable config:
                Word<TimedInput<I>> resetTransPrefix = hypothesis.getPrefix(decomposition.state())
                                                                 .append(TimedInput.step()) // prefix of first stable config that is not entry config
                                                                 .append(decomposition.input()); // successor at $i$ in that config

                Word<TimedInput<I>> suffix = Word.fromLetter(new TimeoutSymbol<>());
                Word<TimedOutput<O>> transSuffixOutput = this.timeOracle.answerQuery(resetTransPrefix, suffix);
                Word<TimedOutput<O>> entryConfigSuffixOutput =
                        this.timeOracle.answerQuery(hypothesis.getLocationPrefix(decomposition.state()), suffix);

                if (transSuffixOutput.equals(entryConfigSuffixOutput)) {
                    LOGGER.debug("Inferred missing reset in non-stable config.");
                    return new MissingResetResult<>(decomposition.state().getLocation(),
                                                    (InputSymbol<I>) decomposition.input());
                }
            }

        }

        // 3: add missing local reset
        LOGGER.debug("Inferred missing one-shot timer from incorrect target at non-delaying input.");
        return this.selectOneShotTimer(decomposition, hypothesis, decomposition.state().getEntryDistance());
    }

}
