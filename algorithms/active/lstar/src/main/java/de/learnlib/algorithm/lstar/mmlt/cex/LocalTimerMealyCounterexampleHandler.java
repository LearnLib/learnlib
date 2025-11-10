package de.learnlib.algorithm.lstar.mmlt.cex;

import java.util.List;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithm.lstar.mmlt.LStarLocalTimerMealy;
import de.learnlib.algorithm.lstar.mmlt.cex.results.CexAnalysisResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.FalseIgnoreResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingDiscriminatorResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingOneShotResult;
import de.learnlib.algorithm.lstar.mmlt.cex.results.MissingResetResult;
import de.learnlib.algorithm.lstar.mmlt.hyp.LocalTimerMealyHypothesis;
import de.learnlib.oracle.AbstractTimedQueryOracle;
import de.learnlib.statistic.container.DummyStatsContainer;
import de.learnlib.statistic.container.LearnerStatsProvider;
import de.learnlib.statistic.container.StatsContainer;
import de.learnlib.symbol_filter.SymbolFilter;
import de.learnlib.symbol_filter.SymbolFilterResponse;
import net.automatalib.automaton.mmlt.MealyTimerInfo;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes a truncated counterexample for a hypothesis MMLT:
 * searches for an extended decomposition, post-processes it, and infers an inaccuracy from it.
 *
 * @param <S> Location type
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocalTimerMealyCounterexampleHandler<S, I, O> implements LearnerStatsProvider {
    private static final Logger logger = LoggerFactory.getLogger(LocalTimerMealyCounterexampleHandler.class);
    private final SymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter;
    private StatsContainer stats = new DummyStatsContainer();

    protected final AbstractTimedQueryOracle<I, O> timeOracle;
    private final LocalTimerMealyCounterexampleDecompositor<S, I, O> decompositor;

    public LocalTimerMealyCounterexampleHandler(AbstractTimedQueryOracle<I, O> timeOracle, AcexAnalyzer acexAnalyzer, @NonNull SymbolFilter<TimedInput<I>, InputSymbol<I>> symbolFilter) {
        this.timeOracle = timeOracle;
        this.decompositor = new LocalTimerMealyCounterexampleDecompositor<>(timeOracle, acexAnalyzer);
        this.symbolFilter = symbolFilter;
    }

    @Override
    public void setStatsContainer(StatsContainer container) {
        this.stats = container;
    }

    public <T> CexAnalysisResult<S, I, O> analyzeInconsistency(LocalTimerMealyOutputInconsistency<I, O> outIncons,
                                                           LocalTimerMealyHypothesis<S, I, T, O> hypothesis) {

        // Search for an extended decomposition:
        var decomposition = decompositor.findExtendedDecomposition(outIncons, hypothesis);
        logger.debug("Found an extended decomposition: {}", decomposition);

        // Post-process the decomposition:
        decomposition = decompositor.postProcessExtendedDecomposition(decomposition, hypothesis);
        logger.debug("Post-processed decomposition: {}", decomposition);

        if (decomposition.isForIncorrectOutput()) {
            return handleIncorrectOutput(decomposition, hypothesis);
        } else {
            return handleIncorrectTarget(decomposition, hypothesis);
        }
    }

    private CexAnalysisResult<S, I, O> handleIncorrectOutput(ExtendedDecomposition<S, I, O> decomposition, LocalTimerMealyHypothesis<S, I, ?, O> hypothesis) {
        // Transition with incorrect output always implies missing one-shot timer:
        logger.debug("Found missing one-shot via incorrect output.");
        return this.selectOneShotTimer(decomposition, hypothesis, decomposition.state().getEntryDistance());
    }

    private CexAnalysisResult<S, I, O> handleIncorrectTarget(ExtendedDecomposition<S, I, O> decomposition, LocalTimerMealyHypothesis<S, I, ?, O> hypothesis) {
        if (decomposition.input() instanceof InputSymbol<I> ndi) {
            // If decomposition at non-delaying input + considered as self-loop, treat as false ignore:
            if (symbolFilter.query(hypothesis.getLocationPrefix(decomposition.state()), ndi) == SymbolFilterResponse.IGNORE) {
                return new FalseIgnoreResult<>(decomposition.state().getLocation(), ndi);
            }

            return this.handleIncorrectTargetNonDelaying(decomposition, hypothesis);

        } else if (decomposition.input() instanceof TimeStepSequence<I>) {
            return this.handleIncorrectTargetTimeStep(decomposition, hypothesis);
        } else {
            throw new AssertionError("Unexpected symbol type.");
        }
    }

    private CexAnalysisResult<S, I, O> selectOneShotTimer(ExtendedDecomposition<S, I, O> decomposition, LocalTimerMealyHypothesis<S, I, ?, O> hypothesis, long maxInitialValue) {
        var newOneShot = LStarLocalTimerMealy.selectOneShotTimer(hypothesis.getSortedTimers(decomposition.state().getLocation()), maxInitialValue);
        logger.debug("Missing one-shot: setting ({}|{}) to one-shot.", hypothesis.getLocationPrefix(decomposition.state()), newOneShot);
        return new MissingOneShotResult<>(decomposition.state().getLocation(), newOneShot);
    }

    private CexAnalysisResult<S, I, O> handleIncorrectTargetTimeStep(ExtendedDecomposition<S, I, O> decomposition, LocalTimerMealyHypothesis<S, I, ?, O> hypothesis) {
        // Check if there is a one-shot timer expiring at the next time step:

        List<MealyTimerInfo<O>> localTimers = hypothesis.getSortedTimers(decomposition.state().getLocation());
        assert !localTimers.isEmpty();

        // If location has a one-shot timer, this is the one with the highest initial value:
        var lastTimer = localTimers.get(localTimers.size() - 1);
        if (!lastTimer.periodic()) {
            if (lastTimer.initial() - 1 != decomposition.state().getEntryDistance()) {
                throw new AssertionError("Incorrect target must be at timeout of non-periodic timer.");
            }
            logger.debug("Inferred missing discriminator at timeout.");
            return new MissingDiscriminatorResult<>(decomposition.state().getLocation(), decomposition.input(), decomposition.discriminator());
        } else if (!decomposition.state().isStableConfig()) {
            logger.debug("Found missing one-shot via incorrect target in non-stable config.");
            return this.selectOneShotTimer(decomposition, hypothesis, decomposition.state().getEntryDistance());
        } else {
            logger.debug("Found missing one-shot via incorrect target in stable config.");
            return new MissingOneShotResult<>(decomposition.state().getLocation(), localTimers.get(0)); // lowest initial value
        }
    }

    private <T> CexAnalysisResult<S, I, O> handleIncorrectTargetNonDelaying(ExtendedDecomposition<S, I, O> decomposition, LocalTimerMealyHypothesis<S, I, T, O> hypothesis) {
        // 1: can be a missing discriminator?

        // Check if correct target in entry w.r.t. discriminator:
        var transPrefix = hypothesis.getLocationPrefix(decomposition.state()).append(decomposition.input());
        var succState = hypothesis.getSemantics().getState(transPrefix); // successor state in hypothesis

        var actualSuffixOutput = this.timeOracle.querySuffixOutput(transPrefix, decomposition.discriminator());
        var expSuffixOutput = this.timeOracle.querySuffixOutput(hypothesis.getPrefix(succState), decomposition.discriminator());

        if (!actualSuffixOutput.equals(expSuffixOutput)) {
            logger.debug("Inferred missing discriminator at non-delaying input.");
            return new MissingDiscriminatorResult<>(decomposition.state().getLocation(), decomposition.input(), decomposition.discriminator());
        }

        // 2: can be a local reset?
        if (decomposition.state().isStableConfig()) {
            logger.debug("Inferred missing reset in stable config.");
            return new MissingResetResult<>(decomposition.state().getLocation(), (InputSymbol<I>) decomposition.input());
        }

        // Non-stable -> explicitly test for missing reset:
        var isLocalReset = hypothesis.isLocalReset(decomposition.state().getLocation(), (InputSymbol<I>) decomposition.input());
        var trans = hypothesis.getTransition(decomposition.state().getLocation(), (InputSymbol<I>) decomposition.input());
        if (trans == null) {
            throw new AssertionError();
        }

        var successor = hypothesis.getSuccessor(trans);

        // Must loop without reset:
        if (successor.equals(decomposition.state().getLocation()) && (!isLocalReset)) {
            // Must have at least two stable configs:
            var firstTimer = hypothesis.getSortedTimers(decomposition.state().getLocation()).get(0);
            if (firstTimer.initial() > 1) {
                // Must not self-loop in at least one non-entry stable config:
                var resetTransPrefix = hypothesis.getPrefix(decomposition.state())
                        .append(TimedInput.step()) // prefix of first stable config that is not entry config
                        .append(decomposition.input()); // successor at $i$ in that config

                Word<TimedInput<I>> suffix = Word.fromLetter(new TimeoutSymbol<>());
                var transSuffixOutput = this.timeOracle.querySuffixOutput(resetTransPrefix, suffix);
                var entryConfigSuffixOutput = this.timeOracle.querySuffixOutput(hypothesis.getLocationPrefix(decomposition.state()), suffix);

                if (transSuffixOutput.equals(entryConfigSuffixOutput)) {
                    logger.debug("Inferred missing reset in non-stable config.");
                    return new MissingResetResult<>(decomposition.state().getLocation(), (InputSymbol<I>) decomposition.input());
                }
            }

        }

        // 3: add missing local reset
        logger.debug("Inferred missing one-shot timer from incorrect target at non-delaying input.");
        return this.selectOneShotTimer(decomposition, hypothesis, decomposition.state().getEntryDistance());
    }


}
