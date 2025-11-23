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
import de.learnlib.sul.SUL;

public class CounterSUL<I, O> implements SUL<I, O> {

    public static final String RESET_KEY = "sul-reset-cnt";
    public static final String SYMBOL_KEY = "sul-step-cnt";

    private final SUL<I, O> sul;
    protected final StatisticsCollector statisticsCollector;
    protected final String id;

    public CounterSUL(SUL<I, O> sul) {
        this(sul, "");
    }

    public CounterSUL(SUL<I, O> sul, String id) {
        this(sul, id, Statistics.getCollector());
    }

    protected CounterSUL(SUL<I, O> sul, String id, StatisticsCollector statisticsCollector) {
        this.sul = sul;
        this.id = id;
        this.statisticsCollector = statisticsCollector;
    }

    @Override
    public void pre() {
        this.statisticsCollector.increaseCounter(RESET_KEY + id, "Number of SUL resets");
        this.sul.pre();
    }

    @Override
    public void post() {
        this.sul.post();
    }

    @Override
    public O step(I in) {
        this.statisticsCollector.increaseCounter(SYMBOL_KEY + id, "Number of SUL steps");
        return sul.step(in);
    }

    @Override
    public boolean canFork() {
        return sul.canFork();
    }

    @Override
    public SUL<I, O> fork() {
        return new CounterSUL<>(this.sul.fork(), this.id, this.statisticsCollector);
    }
}
