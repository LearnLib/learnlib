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

package de.learnlib.filter.statistic.oracle;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.Query;
import de.learnlib.api.statistic.StatisticOracle;
import de.learnlib.filter.statistic.Counter;
import net.automatalib.words.Word;

/**
 * Counts queries.
 *
 * @author falkhowar
 */
@ParametersAreNonnullByDefault
public class CounterOracle<I, D> implements StatisticOracle<I, D> {

    private final Counter counter;
    private MembershipOracle<I, D> nextOracle;

    public CounterOracle(MembershipOracle<I, D> nextOracle, String name) {
        this.nextOracle = nextOracle;
        this.counter = new Counter(name, "queries");
    }

    @Override
    public void processQueries(Collection<? extends Query<I, D>> queries) {
        this.counter.increment(queries.size());
        nextOracle.processQueries(queries);
    }

    @Override
    @Nonnull
    public Counter getStatisticalData() {
        return this.counter;
    }

    @Nonnull
    public Counter getCounter() {
        return this.counter;
    }

    public long getCount() {
        return counter.getCount();
    }

    @Override
    public void setNext(MembershipOracle<I, D> next) {
        this.nextOracle = next;
    }

    public static class DFACounterOracle<I> extends CounterOracle<I, Boolean> implements DFAMembershipOracle<I> {

        public DFACounterOracle(MembershipOracle<I, Boolean> nextOracle, String name) {
            super(nextOracle, name);
        }
    }

    public static class MealyCounterOracle<I, O> extends CounterOracle<I, Word<O>>
            implements MealyMembershipOracle<I, O> {

        public MealyCounterOracle(MembershipOracle<I, Word<O>> nextOracle, String name) {
            super(nextOracle, name);
        }
    }

}
