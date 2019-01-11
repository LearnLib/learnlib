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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.automatalib.automata.transducers.StateLocalInputMealyMachine;

/**
 * Utilities for {@link net.automatalib.automata.transducers.StateLocalInputMealyMachine}s.
 *
 * @author frohme
 */
public final class SLIMMUtil {

    private SLIMMUtil() {
        throw new AssertionError("Should not be instantiated");
    }

    public static <I, O> StateLocalInputMealyMachine<Integer, I, ?, OutputAndLocalInputs<I, O>> partial2StateLocal(
            StateLocalInputMealyMachine<Integer, I, ?, O> reference) {
        return partial2StateLocal(reference, reference.size());
    }

    public static <S, I, T, O> StateLocalInputMealyMachine<S, I, ?, OutputAndLocalInputs<I, O>> partial2StateLocal(
            StateLocalInputMealyMachine<S, I, T, O> reference,
            S sink) {
        return new SLIWrapper<>(reference, sink);
    }

    private static class SLIWrapper<S, I, T, O>
            implements StateLocalInputMealyMachine<S, I, WrapperTransition<S, I, T, O>, OutputAndLocalInputs<I, O>> {

        private final StateLocalInputMealyMachine<S, I, T, O> reference;
        private final List<S> listOfStates;
        private final S sinkState;

        SLIWrapper(StateLocalInputMealyMachine<S, I, T, O> reference, S sink) {
            this.reference = reference;
            this.sinkState = sink;

            this.listOfStates = new ArrayList<>(reference.size() + 1);
            this.listOfStates.addAll(reference.getStates());
            this.listOfStates.add(sinkState);
        }

        @Nonnull
        @Override
        public Collection<S> getStates() {
            return listOfStates;
        }

        @Nullable
        @Override
        public OutputAndLocalInputs<I, O> getTransitionOutput(WrapperTransition<S, I, T, O> transition) {
            return transition.getOutput();
        }

        @Nullable
        @Override
        public WrapperTransition<S, I, T, O> getTransition(S state, @Nullable I input) {
            if (state.equals(sinkState)) {
                return new WrapperTransition<>(null, null);
            }

            return new WrapperTransition<>(reference, reference.getTransition(state, input));
        }

        @Nonnull
        @Override
        public S getSuccessor(WrapperTransition<S, I, T, O> transition) {
            final T trans = transition.getTransition();

            return trans == null ? sinkState : reference.getSuccessor(trans);
        }

        @Nullable
        @Override
        public S getInitialState() {
            return reference.getInitialState();
        }

        @Override
        public Collection<I> getLocalInputs(S state) {
            if (state.equals(sinkState)) {
                return Collections.emptySet();
            }

            return reference.getLocalInputs(state);
        }
    }

    private static class WrapperTransition<S, I, T, O> {

        private final StateLocalInputMealyMachine<S, I, T, O> reference;
        private final T transition;
        private OutputAndLocalInputs<I, O> output;

        WrapperTransition(StateLocalInputMealyMachine<S, I, T, O> reference, T transition) {
            this.reference = reference;
            this.transition = transition;
        }

        public T getTransition() {
            return transition;
        }

        public OutputAndLocalInputs<I, O> getOutput() {
            if (transition == null) {
                return OutputAndLocalInputs.undefined();
            }

            if (output == null) {
                final S succ = reference.getSuccessor(transition);
                final O out = reference.getTransitionOutput(transition);
                output = new OutputAndLocalInputs<>(out, reference.getLocalInputs(succ));
            }

            return output;
        }

    }

}
