/* Copyright (C) 2013-2022 TU Dortmund
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

import java.util.Collection;

import de.learnlib.api.StateLocalInputSUL;
import de.learnlib.filter.statistic.Counter;

public class SymbolCounterStateLocalInputSUL<I, O> extends SymbolCounterSUL<I, O> implements StateLocalInputSUL<I, O> {

    private final StateLocalInputSUL<I, O> sul;

    public SymbolCounterStateLocalInputSUL(String name, StateLocalInputSUL<I, O> sul) {
        super(name, sul);
        this.sul = sul;
    }

    private SymbolCounterStateLocalInputSUL(Counter counter, StateLocalInputSUL<I, O> sul) {
        super(counter, sul);
        this.sul = sul;
    }

    @Override
    public Collection<I> currentlyEnabledInputs() {
        return sul.currentlyEnabledInputs();
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return new SymbolCounterStateLocalInputSUL<>(getStatisticalData(), sul.fork());
    }
}

