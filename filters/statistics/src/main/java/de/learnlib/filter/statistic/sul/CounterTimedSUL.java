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
package de.learnlib.filter.statistic.sul;

import java.util.List;

import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.sul.TimedSUL;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedOutput;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wrapper for a {@link TimedSUL} that gathers various statistics on queries sent to this SUL.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class CounterTimedSUL<I, O> extends CounterSUL<InputSymbol<I>, TimedOutput<O>> implements TimedSUL<I, O> {

    /**
     * The {@link StatisticsKey} this class uses for counting the number of
     * {@link TimeStepSequence#timeSteps() time steps} executed on the SUL.
     */
    public static final StatisticsKey KEY_TIMESTEPS = new StatisticsKey("sul-timestep-cnt", "Number of timed steps");

    private final TimedSUL<I, O> delegate;
    private final StatisticsKey keyTimesteps;

    /**
     * Convenience constructor for {@link CounterTimedSUL#CounterTimedSUL(TimedSUL, String)} which uses {@code null} as
     * {@code id}.
     *
     * @param delegate
     *         the SUL to delegate calls to
     */
    public CounterTimedSUL(TimedSUL<I, O> delegate) {
        this(delegate, null);
    }

    /**
     * Constructs a new counter SUL that writes statistical data to a {@link StatisticsService}. The provided {@code id}
     * is used to refine the supported {@link StatisticsKey}s and allows for using multiple instances of this class for
     * different purposes.
     *
     * @param delegate
     *         the SUL to delegate calls to
     * @param id
     *         the id used for specializing the statistics keys
     */
    public CounterTimedSUL(TimedSUL<I, O> delegate, @Nullable String id) {
        this(delegate, id, Statistics.getService());
    }

    protected CounterTimedSUL(TimedSUL<I, O> delegate, @Nullable String id, StatisticsService statistics) {
        super(delegate, id, statistics);
        this.delegate = delegate;
        this.keyTimesteps = KEY_TIMESTEPS.withId(id); // already incremented by parent
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        TimedOutput<O> res = this.delegate.timeoutStep(maxTime);
        if (res == null) {
            // Waited until maxTime, no timeout occurred:
            super.statistics.increaseCounter(this.keyTimesteps, maxTime, this);
        } else {
            super.statistics.increaseCounter(this.keyTimesteps, res.delay(), this);
        }

        return res;
    }

    @Override
    public List<TimedOutput<O>> collectTimeouts(TimeStepSequence<I> input) {
        super.statistics.increaseCounter(this.keyTimesteps, input.timeSteps(), this);
        return this.delegate.collectTimeouts(input);
    }

    @Override
    public TimedSUL<I, O> fork() {
        return new CounterTimedSUL<>(this.delegate.fork(), super.id, super.statistics);
    }

}
