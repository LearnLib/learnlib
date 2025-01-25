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

import java.util.Collection;

import de.learnlib.sul.StateLocalInputSUL;
import net.automatalib.automaton.transducer.StateLocalInputMealyMachine;

public class StateLocalInputMealySimulatorSUL<I, O> extends MealySimulatorSUL<I, O>
        implements StateLocalInputSUL<I, O> {

    private final SLIMealySimulatorSULImpl<?, I, ?, O> impl;

    public StateLocalInputMealySimulatorSUL(StateLocalInputMealyMachine<?, I, ?, O> mealy) {
        this(new SLIMealySimulatorSULImpl<>(mealy));
    }

    private StateLocalInputMealySimulatorSUL(SLIMealySimulatorSULImpl<?, I, ?, O> impl) {
        super(impl);
        this.impl = impl;
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return new StateLocalInputMealySimulatorSUL<>(impl.fork());
    }

    @Override
    public Collection<I> currentlyEnabledInputs() {
        return this.impl.currentlyEnabledInputs();
    }

    private static final class SLIMealySimulatorSULImpl<S, I, T, O> extends MealySimulatorSULImpl<S, I, T, O>
            implements StateLocalInputSUL<I, O> {

        private final StateLocalInputMealyMachine<S, I, T, O> mealy;

        SLIMealySimulatorSULImpl(StateLocalInputMealyMachine<S, I, T, O> mealy) {
            super(mealy, null);
            this.mealy = mealy;
        }

        @Override
        public Collection<I> currentlyEnabledInputs() {
            return this.mealy.getLocalInputs(super.getCurr());
        }

        @Override
        public SLIMealySimulatorSULImpl<S, I, T, O> fork() {
            return new SLIMealySimulatorSULImpl<>(mealy);
        }
    }
}
