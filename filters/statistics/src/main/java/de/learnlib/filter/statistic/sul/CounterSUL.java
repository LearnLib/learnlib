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

import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.CounterCollection;
import de.learnlib.statistic.StatisticData;
import de.learnlib.statistic.StatisticSUL;
import de.learnlib.sul.SUL;

public class CounterSUL<I, O> implements StatisticSUL<I, O> {

    private final SUL<I, O> sul;
    protected final Counter resetCounter;
    protected final Counter symbolCounter;

    public CounterSUL(SUL<I, O> sul) {
        this(sul, new Counter("Resets", "#"), new Counter("Symbols", "#"));
    }

    protected CounterSUL(SUL<I, O> sul, Counter resetCounter, Counter symbolCounter) {
        this.sul = sul;
        this.resetCounter = resetCounter;
        this.symbolCounter = symbolCounter;
    }

    @Override
    public void pre() {
        this.resetCounter.increment();
        this.sul.pre();
    }

    @Override
    public void post() {
        this.sul.post();
    }

    @Override
    public O step(I in) {
        this.symbolCounter.increment();
        return sul.step(in);
    }

    @Override
    public boolean canFork() {
        return sul.canFork();
    }

    @Override
    public SUL<I, O> fork() {
        return new CounterSUL<>(this.sul.fork(), this.resetCounter, this.symbolCounter);
    }

    @Override
    public StatisticData getStatisticalData() {
        return new CounterCollection(this.resetCounter, this.symbolCounter);
    }

    public Counter getResetCounter() {
        return this.resetCounter;
    }

    public Counter getSymbolCounter() {
        return this.symbolCounter;
    }
}
