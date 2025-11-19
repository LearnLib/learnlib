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

import de.learnlib.sul.StateLocalInputSUL;

public class CounterStateLocalInputSUL<I, O> extends CounterSUL<I, O> implements StateLocalInputSUL<I, O> {

    public static final String INPUT_KEY = "-sul-inp-cnt";

    private final StateLocalInputSUL<I, O> sul;

    public CounterStateLocalInputSUL(StateLocalInputSUL<I, O> sul) {
        this(sul, "");
    }

    private CounterStateLocalInputSUL(StateLocalInputSUL<I, O> sul, String prefix) {
        super(sul, prefix);
        this.sul = sul;
    }

    @Override
    public Collection<I> currentlyEnabledInputs() {
        super.statistics.increaseCounter(prefix + INPUT_KEY, "Number of enabled input checks");
        return this.sul.currentlyEnabledInputs();
    }

    @Override
    public StateLocalInputSUL<I, O> fork() {
        return new CounterStateLocalInputSUL<>(this.sul.fork(), super.prefix);
    }

}
