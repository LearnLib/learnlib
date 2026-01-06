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

import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import de.learnlib.sul.SUL;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wrapper for a {@link SUL} that gathers various statistics on queries sent to this SUL.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class CounterSUL<I, O> implements SUL<I, O> {

    /**
     * The {@link StatisticsKey} this class uses for counting the number of {@link SUL#post() posts} executed on the
     * SUL. This corresponds to the number of queries answered by the SUL.
     */
    public static final StatisticsKey KEY_QUERY = new StatisticsKey("sul-reset-cnt", "Number of SUL posts");

    /**
     * The {@link StatisticsKey} this class uses for counting the number of {@link SUL#step(Object) steps} executed on
     * the SUL. This corresponds to the number of symbols of each query.
     */
    public static final StatisticsKey KEY_SYMBOL = new StatisticsKey("sul-step-cnt", "Number of SUL steps");

    private final SUL<I, O> delegate;
    private final StatisticsKey keyReset;
    private final StatisticsKey keySymbol;

    protected final StatisticsService statistics;
    protected final @Nullable String id;

    private long stepCount;

    /**
     * Convenience constructor for {@link CounterSUL#CounterSUL(SUL, String)} which uses {@code null} as {@code id}.
     *
     * @param delegate
     *         the SUL to delegate calls to
     */
    public CounterSUL(SUL<I, O> delegate) {
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
     *         the id used for specialising the statistics keys
     */
    public CounterSUL(SUL<I, O> delegate, @Nullable String id) {
        this(delegate, id, Statistics.getService());
    }

    protected CounterSUL(SUL<I, O> delegate, @Nullable String id, StatisticsService statistics) {
        this.delegate = delegate;

        this.keyReset = KEY_QUERY.withId(id);
        this.keySymbol = KEY_SYMBOL.withId(id);

        this.statistics = statistics;
        this.id = id;
    }

    @Override
    public void pre() {
        this.stepCount = 0;
        this.delegate.pre();
    }

    @Override
    public void post() {
        this.statistics.increaseCounter(keyReset, this);
        this.statistics.increaseCounter(keySymbol, this.stepCount, this);
        this.delegate.post();
    }

    @Override
    public O step(I in) {
        this.stepCount++;
        return delegate.step(in);
    }

    @Override
    public boolean canFork() {
        return delegate.canFork();
    }

    @Override
    public SUL<I, O> fork() {
        return new CounterSUL<>(this.delegate.fork(), this.id, this.statistics);
    }
}
