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

import java.util.Optional;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.filter.statistic.TestQueries;
import de.learnlib.statistic.Statistics;
import de.learnlib.sul.SUL;
import de.learnlib.sul.TimedSUL;
import net.automatalib.symbol.time.InputSymbol;
import net.automatalib.symbol.time.TimedOutput;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ResetCounterTimedSULTest
        extends AbstractResetCounterSULTest<CounterTimedSUL<Integer, Character>, InputSymbol<Integer>, TimedOutput<Character>> {

    @Override
    protected CounterTimedSUL<Integer, Character> getStatisticSUL() {
        return new CounterTimedSUL<>(new MealyAsMMLTSUL<>(new MealySimulatorSUL<>(TestQueries.DELEGATE)));
    }

    @Override
    protected Optional<Long> getCount(CounterTimedSUL<Integer, Character> sul) {
        return Statistics.getService().getCount(CounterTimedSUL.KEY_QUERY);
    }

    static final class MealyAsMMLTSUL<I, O> implements TimedSUL<I, O> {

        private final SUL<I, O> delegate;

        MealyAsMMLTSUL(SUL<I, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public @Nullable TimedOutput<O> timeoutStep(long maxTime) {
            return null;
        }

        @Override
        public void pre() {
            this.delegate.pre();
        }

        @Override
        public void post() {
            this.delegate.post();
        }

        @Override
        public TimedOutput<O> step(InputSymbol<I> in) {
            return new TimedOutput<>(this.delegate.step(in.symbol()));
        }

        @Override
        public TimedSUL<I, O> fork() {
            return new MealyAsMMLTSUL<>(this.delegate.fork());
        }

        @Override
        public boolean canFork() {
            return this.delegate.canFork();
        }
    }
}
