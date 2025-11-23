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
package de.learnlib.filter.statistic.sul;

import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsCollector;
import de.learnlib.sul.TimedSUL;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimeStepSequence;
import net.automatalib.symbol.time.TimedOutput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wrapper for a {@link TimedSUL} that gathers various statistics on queries sent to this SUL.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class CounterTimedSUL<I, O> implements TimedSUL<I, O> {

    private final TimedSUL<I, O> delegate;
    private final StatisticsCollector stats;

    private final @Nullable String name;

    public CounterTimedSUL(TimedSUL<I, O> delegate) {
        this(delegate, null);
    }

    public CounterTimedSUL(TimedSUL<I, O> delegate, String name) {
        this(delegate, name, Statistics.getCollector());
    }

    protected CounterTimedSUL(TimedSUL<I, O> delegate, String name, StatisticsCollector statistics) {
        this.delegate = delegate;
        this.name = name;
        this.stats = statistics;
    }

    private String withPrefix(String label) {
        if (this.name == null) {
            return label;
        }
        return this.name + ":" + label;
    }

    @Override
    public TimedOutput<O> step(InputSymbol<I> input) {
        stats.increaseCounter(withPrefix("sul_untimed_syms_counter"), withPrefix("Total untimed symbols"));
        return this.delegate.step(input);
    }

    @Override
    public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
        TimedOutput<O> res = this.delegate.timeoutStep(maxTime);
        if (res == null) {
            // Waited until maxTime, no timeout occurred:
            stats.increaseCounter(withPrefix("sul_total_time"), withPrefix("Total query time"), maxTime);
        } else {
            stats.increaseCounter(withPrefix("sul_total_time"), withPrefix("Total query time"), res.delay());
        }

        return res;
    }

    @Override
    public Word<TimedOutput<O>> collectTimeouts(TimeStepSequence<I> input) {
        stats.increaseCounter(withPrefix("sul_total_time"), withPrefix("Total query time"), input.timeSteps());
        return this.delegate.collectTimeouts(input);
    }

    @Override
    public void pre() {
        this.delegate.pre();
        stats.increaseCounter(withPrefix("sul_resets_counter"), withPrefix("SUL resets"));
    }

    @Override
    public void post() {
        this.delegate.post();
    }

    @Override
    public boolean canFork() {
        return this.delegate.canFork();
    }

    @Override
    public TimedSUL<I, O> fork() {
        return new CounterTimedSUL<>(this.delegate.fork(), this.name, this.stats);
    }

}
