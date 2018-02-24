/* Copyright (C) 2013-2018 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.SUL;
import de.learnlib.api.exception.SULException;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.filter.statistic.Counter;

@ParametersAreNonnullByDefault
public class ResetCounterSUL<I, O> implements StatisticSUL<I, O> {

    private final SUL<I, O> sul;
    private final Counter counter;

    public ResetCounterSUL(String name, SUL<I, O> sul) {
        this(new Counter(name, "resets"), sul);
    }

    protected ResetCounterSUL(Counter counter, SUL<I, O> sul) {
        this.counter = counter;
        this.sul = sul;
    }

    @Override
    public void pre() {
        counter.increment();
        sul.pre();
    }

    @Override
    public void post() {
        sul.post();
    }

    @Override
    @Nullable
    public O step(@Nullable I in) throws SULException {
        return sul.step(in);
    }

    @Override
    public boolean canFork() {
        return sul.canFork();
    }

    @Override
    public SUL<I, O> fork() {
        return new ResetCounterSUL<>(counter, sul.fork());
    }

    @Override
    @Nonnull
    public Counter getStatisticalData() {
        return counter;
    }

    @Override
    public boolean equals(Object obj) {
        return sul.equals(obj);
    }

    @Override
    public int hashCode() {
        return sul.hashCode();
    }

    @Override
    public String toString() {
        return sul.toString();
    }
}
