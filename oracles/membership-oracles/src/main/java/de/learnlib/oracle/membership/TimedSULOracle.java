/* Copyright (C) 2013-2025 TU Dortmund University
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
package de.learnlib.oracle.membership;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.learnlib.oracle.SingleQueryOracle.SingleQueryOracleMMLT;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.sul.TimedSUL;
import de.learnlib.time.MMLTModelParams;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.symbol.time.TimeoutSymbol;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a {@link TimedQueryOracle} given a {@link TimedSUL}.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class TimedSULOracle<I, O> implements SingleQueryOracleMMLT<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimedSULOracle.class);

    /**
     * To ensure globally unique timer names, we index them according to this counter.
     */
    private int timerCounter;

    private final TimedSUL<I, O> sul;
    private final MMLTModelParams<O> modelParams;

    public TimedSULOracle(TimedSUL<I, O> sul, MMLTModelParams<O> modelParams) {
        this.sul = sul;
        this.modelParams = modelParams;
        this.timerCounter = 0;
    }

    @Override
    public Word<TimedOutput<O>> answerQuery(Word<TimedInput<I>> prefix, Word<TimedInput<I>> suffix) {
        sul.pre();
        sul.follow(prefix, this.modelParams.maxTimeoutWaitingTime());

        // Query the SUL, one symbol at a time:
        WordBuilder<TimedOutput<O>> wbOutput = new WordBuilder<>();
        for (TimedInput<I> s : suffix) {
            if (s instanceof TimeoutSymbol<I>) {
                TimedOutput<O> output = sul.timeoutStep(this.modelParams.maxTimeoutWaitingTime());
                if (output != null) {
                    wbOutput.append(output);
                } else {
                    wbOutput.append(new TimedOutput<>(this.modelParams.silentOutput())); // no output in time -> silent
                }
            } else if (s instanceof InputSymbol<I> ndi) {
                TimedOutput<O> output = sul.step(ndi);
                wbOutput.append(output);
            } else if (s instanceof TimeStepSequence<I> ws) {
                if (ws.timeSteps() > 1) {
                    throw new IllegalArgumentException("Only single wait step allowed in suffix.");
                }

                // Wait for a single time step:
                TimedOutput<O> output = sul.timeStep();
                if (output != null) {
                    wbOutput.append(output);
                } else {
                    wbOutput.append(new TimedOutput<>(this.modelParams.silentOutput())); // no output in time -> silent
                }

            } else {
                throw new IllegalArgumentException("Only timeout or untimed symbols allowed in suffix.");
            }
        }

        sul.post();
        return wbOutput.toWord();
    }

    @Override
    public TimerQueryResult<O> queryTimers(Word<TimedInput<I>> prefix, long maxTotalWaitingTime) {
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
     * @param timeouts
     *         the known timeouts
     * @param currentTime
     *         the current time
     *
     * @return the next timeout time
     */
    private long calcNextExpectedTimeout(List<TimerInfo<?, O>> timeouts, long currentTime) {
        assert !timeouts.isEmpty();

        long minNext = Long.MAX_VALUE;
        for (TimerInfo<?, O> to : timeouts) {
            long occurrences = currentTime / to.initial();
            long nextOcc = (occurrences + 1) * to.initial(); // time of next occ

            if (nextOcc < minNext) {
                minNext = nextOcc;
            }
        }

        assert minNext != Long.MAX_VALUE;

        return minNext;
    }

    private String newUniqueTimerName() {
        return "t_" + (++this.timerCounter);
    }

    /**
     * Identifies timeouts in the current location by waiting at most {@code maxTotalWaitingTime}.
     * <p>
     * All inferred timers are initially considered periodic. Stops when reaching {@code maxTotalWaitingTime} or when an
     * expected timeout does not occur. In the latter case, the {@link TimerQueryResult#aborted()}} flag is set.
     *
     * @param maxTotalWaitingTime
     *         the maximum time until timeouts are collected
     *
     * @return the list of periodic timeouts or {@code null} if none observed
     */
    private TimerQueryResult<O> collectTimeouts(long maxTotalWaitingTime) {
        if (maxTotalWaitingTime < this.modelParams.maxTimeoutWaitingTime()) {
            throw new IllegalArgumentException(
                    "Timer query waiting time must be at least maximum waiting time for a single timeout.");
        }

        List<TimerInfo<?, O>> knownTimers = new ArrayList<>();

        // Wait for the first timeout:
        TimedOutput<O> firstTimeout = this.sul.timeoutStep(this.modelParams.maxTimeoutWaitingTime());
        if (firstTimeout == null) {
            return new TimerQueryResult<>(false, Collections.emptyList()); // no timeouts found
        }

        List<O> firstTimeoutOutputs = this.modelParams.outputCombiner().separateSymbols(firstTimeout.symbol());
        if (firstTimeoutOutputs.size() > 1) {
            LOGGER.warn("Multiple timers expiring at first timeout, automaton may not be minimal.");
        }

        knownTimers.add(new TimerInfo<>(newUniqueTimerName(), firstTimeout.delay(), firstTimeoutOutputs, null, true));

        // Wait for further timeouts:
        long currentTimeStep = firstTimeout.delay(); // already waited for first timeout

        boolean inconsistent = false;
        while (currentTimeStep < maxTotalWaitingTime) {
            // Identify time of next expected timeout:
            long nextExpectedTime = this.calcNextExpectedTimeout(knownTimers, currentTimeStep);

            // Wait either until next timeout OR until maximum waiting time reached:
            long nextWaiting = Math.min(nextExpectedTime, maxTotalWaitingTime) - currentTimeStep;

            // Wait until next timeout:
            TimedOutput<O> nextOutput = this.sul.timeoutStep(nextWaiting);
            if (nextOutput == null) {
                if (nextExpectedTime <= maxTotalWaitingTime) {
                    // Expected a timeout within maximum waiting time but nothing happened:
                    inconsistent = true;
                }

                break; // either max time exceeded OR missing timeout (-> inconsistent)
            }

            // Compare observed timeout with expectation:
            long nextActualTime = nextOutput.delay() + currentTimeStep;

            TimerCheckResult<O> evalResult =
                    evaluateNextTimer(nextActualTime, nextExpectedTime, nextOutput, knownTimers);
            if (evalResult.newTimer() != null) {
                knownTimers.add(evalResult.newTimer());
            } else if (evalResult.inconsistent()) {
                inconsistent = true;
                break;
            }

            currentTimeStep = nextActualTime;
        }

        knownTimers.sort(Comparator.comparingLong(TimerInfo::initial));
        return new TimerQueryResult<>(inconsistent, knownTimers);
    }

    private TimerCheckResult<O> evaluateNextTimer(long nextActualTime,
                                                  long nextExpectedTime,
                                                  TimedOutput<O> nextOutput,
                                                  List<TimerInfo<?, O>> knownTimers) {

        List<O> nextOutputSymbols = this.modelParams.outputCombiner().separateSymbols(nextOutput.symbol());

        if (nextActualTime < nextExpectedTime) {
            // A timeout occurred before we expected one -> new timer:
            TimerInfo<?, O> newTimer =
                    new TimerInfo<>(newUniqueTimerName(), nextActualTime, nextOutputSymbols, null, true);
            return new TimerCheckResult<>(newTimer, false);
        } else {
            assert nextActualTime == nextExpectedTime;
            // Timeout occurred at expected time -> check if matching expected output:
            Map<O, Long> expectedOutputs = new HashMap<>();
            for (TimerInfo<?, O> t : knownTimers) {
                if (nextExpectedTime % t.initial() == 0) {
                    for (O o : t.outputs()) {
                        expectedOutputs.merge(o, 1L, Long::sum);
                    }
                }
            }

            Map<O, Long> actualOutputs = new HashMap<>();
            for (O o : nextOutputSymbols) {
                actualOutputs.merge(o, 1L, Long::sum);
            }

            // Any outputs that were expected but are not present?
            for (Entry<O, Long> e : expectedOutputs.entrySet()) {
                if (actualOutputs.getOrDefault(e.getKey(), 0L) < e.getValue()) {
                    // Same time but missing output -> missed location change:
                    return new TimerCheckResult<>(null, true);
                }
            }

            // At least all expected outputs are present.
            // Check for additional outputs:
            List<O> newOutputs = new ArrayList<>();
            for (Entry<O, Long> e : actualOutputs.entrySet()) {
                O output = e.getKey();
                long expectedCount = expectedOutputs.getOrDefault(output, 0L);
                long actualCount = e.getValue();

                long additional = actualCount - expectedCount;
                for (int i = 0; i < additional; i++) {
                    newOutputs.add(output);
                }
            }

            if (!newOutputs.isEmpty()) {
                // Same time and more outputs -> add new timer that uses the new outputs:
                TimerInfo<?, O> newTimer =
                        new TimerInfo<>(newUniqueTimerName(), nextActualTime, newOutputs, null, true);
                return new TimerCheckResult<>(newTimer, false);
            }
        }

        return new TimerCheckResult<>(null, false);
    }

    private record TimerCheckResult<O>(@Nullable TimerInfo<?, O> newTimer, boolean inconsistent) {}
}
