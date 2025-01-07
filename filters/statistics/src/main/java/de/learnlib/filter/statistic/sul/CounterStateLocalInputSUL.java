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

import java.util.Collection;

import de.learnlib.filter.statistic.Counter;
import de.learnlib.filter.statistic.CounterCollection;
import de.learnlib.statistic.StatisticData;
import de.learnlib.sul.StateLocalInputSUL;

public class CounterStateLocalInputSUL<I, O> extends CounterSUL<I, O> implements StateLocalInputSUL<I, O> {

    private final StateLocalInputSUL<I, O> sul;
    private final Counter inputCounter;

    public CounterStateLocalInputSUL(StateLocalInputSUL<I, O> sul) {
        super(sul);
        this.sul = sul;
        this.inputCounter = new Counter("Input Checks", "#");
    }

    private CounterStateLocalInputSUL(StateLocalInputSUL<I, O> sul,
                                      Counter resetCounter,
                                      Counter symbolCounter,
                                      Counter inputCounter) {
        super(sul, resetCounter, symbolCounter);
        this.sul = sul;
        this.inputCounter = inputCounter;
    }

    @Override
    public Collection<I> currentlyEnabledInputs() {
        this.inputCounter.increment();
        return this.sul.currentlyEnabledInputs();
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return new CounterStateLocalInputSUL<>(this.sul.fork(),
                                               super.resetCounter,
                                               super.symbolCounter,
                                               this.inputCounter);
    }

    @Override
    public StatisticData getStatisticalData() {
        return new CounterCollection(super.resetCounter, super.symbolCounter, this.inputCounter);
    }

    public Counter getInputCounter() {
        return this.inputCounter;
    }
}
