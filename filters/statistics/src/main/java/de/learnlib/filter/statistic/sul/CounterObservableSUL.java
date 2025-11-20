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
import de.learnlib.statistic.StatsContainer;
import de.learnlib.sul.ObservableSUL;

public class CounterObservableSUL<S, I, O> extends CounterSUL<I, O> implements ObservableSUL<S, I, O> {

    private final ObservableSUL<S, I, O> sul;

    public CounterObservableSUL(ObservableSUL<S, I, O> sul) {
        this(sul, "");
    }

    public CounterObservableSUL(ObservableSUL<S, I, O> sul, String prefix) {
        this(sul, prefix, Statistics.getContainer());
    }

    protected CounterObservableSUL(ObservableSUL<S, I, O> sul, String prefix, StatsContainer statistics) {
        super(sul, prefix, statistics);
        this.sul = sul;
    }

    @Override
    public ObservableSUL<S, I, O> fork() {
        return new CounterObservableSUL<>(this.sul.fork(), super.prefix, super.statistics);
    }

    @Override
    public S getState() {
        return this.sul.getState();
    }

    @Override
    public boolean deepCopies() {
        return this.sul.deepCopies();
    }
}
