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
package de.learnlib.driver.simulator;

import de.learnlib.sul.ObservableSUL;
import net.automatalib.automaton.transducer.MealyMachine;

public class ObservableMealySimulatorSUL<S, I, O> extends MealySimulatorSUL<I, O> implements ObservableSUL<S, I, O> {

    private final ObservableMealySimulatorSULImpl<S, I, ?, O> impl;

    public ObservableMealySimulatorSUL(MealyMachine<S, I, ?, O> mealy) {
        this(mealy, null);
    }

    public ObservableMealySimulatorSUL(MealyMachine<S, I, ?, O> mealy, O noTransOut) {
        this(new ObservableMealySimulatorSULImpl<>(mealy, noTransOut));
    }

    private ObservableMealySimulatorSUL(ObservableMealySimulatorSULImpl<S, I, ?, O> impl) {
        super(impl);
        this.impl = impl;
    }

    @Override
    public S getState() {
        return impl.getState();
    }

    @Override
    public ObservableSUL<S, I, O> fork() {
        return impl.fork();
    }

    private static final class ObservableMealySimulatorSULImpl<S, I, T, O> extends MealySimulatorSULImpl<S, I, T, O>
            implements ObservableSUL<S, I, O> {

        private final MealyMachine<S, I, T, O> mealy;
        private final O noTransOut;

        ObservableMealySimulatorSULImpl(MealyMachine<S, I, T, O> mealy, O noTransOut) {
            super(mealy, noTransOut);
            this.mealy = mealy;
            this.noTransOut = noTransOut;
        }

        @Override
        public S getState() {
            return getCurr();
        }

        @Override
        public ObservableMealySimulatorSULImpl<S, I, T, O> fork() {
            return new ObservableMealySimulatorSULImpl<>(mealy, noTransOut);
        }

    }
}
