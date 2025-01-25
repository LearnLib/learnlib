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
package de.learnlib.acex;

import net.automatalib.common.util.array.ArrayStorage;
import org.checkerframework.checker.initialization.qual.UnknownInitialization;

public abstract class AbstractBaseCounterexample<E> implements AbstractCounterexample<E> {

    private final ArrayStorage<E> values;

    /**
     * Constructor.
     *
     * @param m
     *         length of the counterexample
     */
    public AbstractBaseCounterexample(int m) {
        this.values = new ArrayStorage<>(m);
    }

    /**
     * Retrieves the length of the abstract counterexample.
     *
     * @return the length of the counterexample
     */
    @Override
    public int getLength() {
        return values.size();
    }

    @Override
    public E effect(int index) {
        E eff = values.get(index);
        if (eff == null) {
            eff = computeEffect(index);
            values.set(index, eff);
        }
        return eff;
    }

    protected abstract E computeEffect(int index);

    public void setEffect(@UnknownInitialization(AbstractBaseCounterexample.class) AbstractBaseCounterexample<E> this,
                          int index,
                          E effect) {
        values.set(index, effect);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(values.size());

        boolean first = true;
        for (E v : values) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            if (v == null) {
                sb.append('?');
            } else {
                sb.append(v);
            }
        }

        return sb.toString();
    }

}
