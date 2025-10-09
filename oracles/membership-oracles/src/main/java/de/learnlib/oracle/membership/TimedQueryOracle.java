package de.learnlib.oracle.membership;

import de.learnlib.algorithm.LocalTimerMealyModelParams;
import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.LocalTimerMealySUL;
import net.automatalib.alphabet.time.mmlt.*;
import net.automatalib.automaton.time.mmlt.MealyTimerInfo;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a timed query oracle for MMLT learning.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class TimedQueryOracle<I, O> extends de.learnlib.oracle.TimedQueryOracle<I, O> {

    private final static Logger logger = LoggerFactory.getLogger(TimedQueryOracle.class);

    // List of all possible timer names.
    // Each name is assigned at most once while this oracle exists. This ensures globally-unique timer names.
    private final List<String> timerNames;
    private int timerNameIndex = 0;

    private final LocalTimerMealySUL<I, O> sul;
    private final LocalTimerMealyModelParams<O> modelParams;

    public TimedQueryOracle(LocalTimerMealySUL<I, O> sul, LocalTimerMealyModelParams<O> modelParams) {
        this.sul = sul;
        this.modelParams = modelParams;

        this.timerNames = generateTimerNames();
    }

    private List<String> generateTimerNames() {
        List<String> names = new ArrayList<>();
        // Add single letter names
        for (char c = 'a'; c <= 'z'; c++) {
            names.add(String.valueOf(c));
        }
        // Add double letter names
        for (char c1 = 'a'; c1 <= 'z'; c1++) {
            for (char c2 = 'a'; c2 <= 'z'; c2++) {
                names.add("" + c1 + c2);
            }
        }
        return names;
    }

    /**
     * Observes and aggregates any timeouts that occur after providing the given input to the SUL.
     * Stops when observing inconsistent behavior.
     *
     * @param prefix              Input to give to the SUL.
     * @param maxTotalWaitingTime Maximum time that is waited for timeouts.
     * @return Observed timeouts. Empty, if none.
     */
    @Override
    public TimerQueryResult<O> queryTimers(Word<LocalTimerMealySemanticInputSymbol<I>> prefix, long maxTotalWaitingTime) {
        this.sul.pre();

        // Go to location:
        this.sul.follow(prefix);

        // Collect timeouts:
        TimerQueryResult<O> timers = this.collectTimeouts(maxTotalWaitingTime);

        this.sul.post();
        return timers;
    }

    /**
     * Identifies the time at which the next known timeout(s) are expected.
     *
     * @param timeouts    Known timeouts
     * @param currentTime Current time.
     * @return Next timeout time.
     */
    private long calcNextExpectedTimeout(List<MealyTimerInfo<O>> timeouts, long currentTime) {
        if (timeouts.isEmpty()) {
            throw new AssertionError();
        }

        long minNext = Long.MAX_VALUE;
        for (var to : timeouts) {
            long occurrences = currentTime / to.initial();
            long nextOcc = (occurrences + 1) * to.initial(); // time of next occ

            if (nextOcc < minNext) {
                minNext = nextOcc;
            }
        }

        if (minNext == Long.MAX_VALUE) {
            throw new AssertionError();
        }

        return minNext;
    }

    private String getUniqueTimerName() {
        var newTimerName = timerNames.get(timerNameIndex);
        this.timerNameIndex += 1;
        return newTimerName;
    }

    /**
     * Identifies timeouts in the current location by waiting at most [maxDelay].
     * <p>
     * All inferred timers are initially considered periodic.
     * Stops when reaching maxTotalWaitingTime OR when an expected timeout does not occur.
     * In the latter case, the "aborted" flag is set.
     *
     * @param maxTotalWaitingTime Maximum time until timeouts are collected.
     * @return List of periodic timeouts. Null, if none observed.
     */
    private TimerQueryResult<O> collectTimeouts(long maxTotalWaitingTime) {
        if (maxTotalWaitingTime < this.modelParams.maxTimeoutWaitingTime()) {
            throw new IllegalArgumentException("Timer query waiting time must be at least max. waiting time for a single timeout.");
        }

        List<MealyTimerInfo<O>> knownTimers = new ArrayList<>();

        // Wait for the first timeout:
        LocalTimerMealyOutputSymbol<O> firstTimeout = this.sul.timeoutStep(this.modelParams.maxTimeoutWaitingTime());
        if (firstTimeout == null) {
            return new TimerQueryResult<>(false, Collections.emptyList()); // no timeouts found
        }

        if (this.modelParams.outputCombiner().isCombinedSymbol(firstTimeout.getSymbol())) {
            logger.warn("Multiple timers expiring at first timeout, automaton may not be minimal.");
        }

        knownTimers.add(new MealyTimerInfo<>(getUniqueTimerName(), firstTimeout.getDelay(), firstTimeout.getSymbol()));

        // Wait for further timeouts:
        long currentTimeStep = firstTimeout.getDelay(); // already waited for first timeout

        boolean inconsistent = false;
        while (currentTimeStep < maxTotalWaitingTime) {
            // Identify time of next expected timeout:
            long nextExpectedTime = this.calcNextExpectedTimeout(knownTimers, currentTimeStep);

            // Wait either until next timeout OR until maximum waiting time reached:
            long nextWaiting = Math.min(nextExpectedTime, maxTotalWaitingTime) - currentTimeStep;

            // Wait until next timeout:
            LocalTimerMealyOutputSymbol<O> nextOutput = this.sul.timeoutStep(nextWaiting);
            if (nextOutput == null) {
                if (nextExpectedTime <= maxTotalWaitingTime) {
                    // Expected a timeout within max. waiting time but nothing happened:
                    inconsistent = true;
                }

                break; // either max time exceeded OR missing timeout (-> inconsistent)
            }

            // Compare observed timeout with expectation:
            long nextActualTime = nextOutput.getDelay() + currentTimeStep;

            TimerCheckResult<O> evalResult = evaluateNextTimer(nextActualTime, nextExpectedTime, nextOutput, knownTimers);
            if (evalResult.newTimer() != null) {
                knownTimers.add(evalResult.newTimer());
            } else if (evalResult.inconsistent()) {
                inconsistent = true;
                break;
            }

            currentTimeStep = nextActualTime;
        }

        knownTimers.sort(Comparator.comparingLong(MealyTimerInfo::initial));
        return new TimerQueryResult<>(inconsistent, knownTimers);
    }

    private record TimerCheckResult<O>(@Nullable MealyTimerInfo<O> newTimer, boolean inconsistent) {

    }

    private TimerCheckResult<O> evaluateNextTimer(long nextActualTime, long nextExpectedTime, LocalTimerMealyOutputSymbol<O> nextOutput, List<MealyTimerInfo<O>> knownTimers) {
        if (nextActualTime < nextExpectedTime) {
            // A timeout occurred before we expected one -> new timer:
            var newTimer = new MealyTimerInfo<>(getUniqueTimerName(), nextActualTime, nextOutput.getSymbol());
            return new TimerCheckResult<>(newTimer, false);
        } else if (nextActualTime == nextExpectedTime) {
            // Timeout occurred at expected time -> check if matching expected output:
            Map<O, Long> expectedOutputs = knownTimers.stream()
                    .filter(t -> nextExpectedTime % t.initial() == 0)
                    .map(t -> modelParams.outputCombiner().separateSymbols(t.output())) // separate output of timers with same initial value
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(t -> t, Collectors.counting())); // count occurrences

            Map<O, Long> actualOutputs = this.modelParams.outputCombiner().separateSymbols(nextOutput.getSymbol())
                    .stream()
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()));

            // Any missing outputs?
            boolean missingOutputs = expectedOutputs.keySet().stream()
                    .anyMatch(o -> actualOutputs.getOrDefault(o, 0L) < expectedOutputs.get(o)); // less than expected
            if (missingOutputs) {
                // Same time but missing output -> missed location change:
                return new TimerCheckResult<>(null, true);
            }

            // Any new outputs?
            List<O> newOutputs = actualOutputs.keySet().stream()
                    .filter(o -> expectedOutputs.getOrDefault(o, 0L) < actualOutputs.get(o)) // less than actual
                    .toList();
            if (!newOutputs.isEmpty()) {
                // Same time and more outputs -> add new timer that uses the new outputs:
                var newTimer = new MealyTimerInfo<>(getUniqueTimerName(), nextActualTime, this.modelParams.outputCombiner().combineSymbols(newOutputs));
                return new TimerCheckResult<>(newTimer, false);
            }
        } else {
            throw new IllegalStateException();
        }

        return new TimerCheckResult<>(null, false);
    }


    @Override
    protected void querySuffixOutputInternal(DefaultQuery<LocalTimerMealySemanticInputSymbol<I>, Word<LocalTimerMealyOutputSymbol<O>>> query) {

        sul.pre();
        sul.follow(query.getPrefix(), this.modelParams.maxTimeoutWaitingTime());

        // Query the SUL, one symbol at a time:
        WordBuilder<LocalTimerMealyOutputSymbol<O>> wbOutput = new WordBuilder<>();
        for (var s : query.getSuffix()) {
            if (s instanceof TimeoutSymbol<I>) {
                LocalTimerMealyOutputSymbol<O> output = sul.timeoutStep(this.modelParams.maxTimeoutWaitingTime());
                if (output != null) {
                    wbOutput.append(output);
                } else {
                    wbOutput.append(new LocalTimerMealyOutputSymbol<>(this.modelParams.silentOutput())); // no output in time -> silent
                }
            } else if (s instanceof NonDelayingInput<I> ndi) {
                LocalTimerMealyOutputSymbol<O> output = sul.step(ndi);
                wbOutput.append(output);
            } else if (s instanceof TimeStepSequence<I> ws) {
                if (ws.getTimeSteps() > 1) {
                    throw new IllegalArgumentException("Only single wait step allowed in suffix.");
                }

                // Wait for a single time step:
                LocalTimerMealyOutputSymbol<O> output = sul.timeStep();
                if (output != null) {
                    wbOutput.append(output);
                } else {
                    wbOutput.append(new LocalTimerMealyOutputSymbol<>(this.modelParams.silentOutput())); // no output in time -> silent
                }

            } else {
                throw new IllegalArgumentException("Only timeout or untimed symbols allowed in suffix.");
            }
        }


        sul.post();
        query.answer(wbOutput.toWord());
    }

}
