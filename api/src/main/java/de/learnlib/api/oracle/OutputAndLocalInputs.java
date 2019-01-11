/* Copyright (C) 2013-2019 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
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
package de.learnlib.api.oracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import net.automatalib.automata.transducers.MealyMachine;

/**
 * A utility class for bundling information about the output of a system and the local inputs of the reached state.
 * Useful when trying to transform a partial {@link MealyMachine} to a complete automaton, while wanting to preserve the
 * information about the partiality.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Maren Geske
 * @author frohme
 * @see net.automatalib.automata.transducers.MealyMachine
 * @see net.automatalib.automata.concepts.StateLocalInput
 */
public final class OutputAndLocalInputs<I, O> {

    private static final OutputAndLocalInputs<?, ?> UNDEFINED =
            new OutputAndLocalInputs<>(null, Collections.emptyList());

    private final Collection<I> localInputs;
    private final O output;

    public OutputAndLocalInputs(O output, Collection<? extends I> localInputs) {
        this.localInputs = new ArrayList<>(localInputs);
        this.output = output;
    }

    @SuppressWarnings("unchecked")
    public static <I, O> OutputAndLocalInputs<I, O> undefined() {
        return (OutputAndLocalInputs<I, O>) UNDEFINED;
    }

    public Collection<I> getLocalInputs() {
        return localInputs;
    }

    public O getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "{output: " + output + ", inputs: " + localInputs + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OutputAndLocalInputs)) {
            return false;
        }
        final OutputAndLocalInputs<?, ?> that = (OutputAndLocalInputs<?, ?>) o;
        return Objects.equals(output, that.output) && Objects.equals(localInputs, that.localInputs);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(output);
        result = 31 * result + Objects.hashCode(localInputs);
        return result;
    }
}