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
import de.learnlib.sul.ObservableSUL;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Wrapper for a {@link ObservableSUL} that gathers various statistics on queries sent to this SUL.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class CounterObservableSUL<S, I, O> extends CounterSUL<I, O> implements ObservableSUL<S, I, O> {

    private final ObservableSUL<S, I, O> delegate;

    /**
     * Convenience constructor for {@link CounterObservableSUL#CounterObservableSUL(ObservableSUL, String)} which uses
     * {@code null} as {@code id}.
     *
     * @param delegate
     *         the SUL to delegate calls to
     */
    public CounterObservableSUL(ObservableSUL<S, I, O> delegate) {
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
    public CounterObservableSUL(ObservableSUL<S, I, O> delegate, @Nullable String id) {
        this(delegate, id, Statistics.getService());
    }

    protected CounterObservableSUL(ObservableSUL<S, I, O> delegate, @Nullable String id, StatisticsService statistics) {
        super(delegate, id, statistics);
        this.delegate = delegate;
    }

    @Override
    public ObservableSUL<S, I, O> fork() {
        return new CounterObservableSUL<>(this.delegate.fork(), super.id, super.statistics);
    }

    @Override
    public S getState() {
        return this.delegate.getState();
    }

    @Override
    public boolean deepCopies() {
        return this.delegate.deepCopies();
    }
}
