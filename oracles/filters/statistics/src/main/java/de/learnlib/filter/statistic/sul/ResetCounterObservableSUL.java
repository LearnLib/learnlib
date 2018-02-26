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
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.ObservableSUL;
import de.learnlib.filter.statistic.Counter;

@ParametersAreNonnullByDefault
public class ResetCounterObservableSUL<S, I, O> extends ResetCounterSUL<I, O> implements ObservableSUL<S, I, O> {

    private final ObservableSUL<S, I, O> sul;

    public ResetCounterObservableSUL(String name, ObservableSUL<S, I, O> sul) {
        super(name, sul);
        this.sul = sul;
    }

    private ResetCounterObservableSUL(Counter counter, ObservableSUL<S, I, O> sul) {
        super(counter, sul);
        this.sul = sul;
    }

    @Override
    public ObservableSUL<S, I, O> fork() {
        return new ResetCounterObservableSUL<>(getStatisticalData(), sul.fork());
    }

    @Nonnull
    @Override
    public S getState() {
        return sul.getState();
    }

    @Override
    public boolean deepCopies() {
        return sul.deepCopies();
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
