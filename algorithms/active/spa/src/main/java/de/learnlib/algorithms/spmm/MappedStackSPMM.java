/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithms.spmm;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.sba.SymbolWrapper;
import net.automatalib.automata.spmm.SPMM;
import net.automatalib.automata.spmm.StackSPMMState;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.MealyTransition;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.words.SPAAlphabet;
import net.automatalib.words.SPAOutputAlphabet;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A stack-based implementation for the (instrumented) semantics of a System of Procedural Automata.
 *
 * @param <S>
 *         hypotheses state type
 * @param <I>
 *         input symbol type
 *
 * @author frohme
 */
public class MappedStackSPMM<S, I, T, O>
        implements SPMM<StackSPMMState<S, SymbolWrapper<I>, T, O>, I, MealyTransition<StackSPMMState<S, SymbolWrapper<I>, T, O>, O>, O> {

    private final SPAAlphabet<I> inputAlphabet;
    private final SPAOutputAlphabet<O> outputAlphabet;

    private final I initialCall;
    private final O initialOutput;
    private final Map<I, MealyMachine<S, SymbolWrapper<I>, T, O>> procedures;
    private final Mapping<I, SymbolWrapper<I>> mapping;

    @SuppressWarnings("unchecked")
    public MappedStackSPMM(SPAAlphabet<I> inputAlphabet,
                           SPAOutputAlphabet<O> outputAlphabet,
                           I initialCall,
                           O initialOutput,
                           Map<I, ? extends MealyMachine<? extends S, SymbolWrapper<I>, ? extends T, O>> procedures,
                           Mapping<I, SymbolWrapper<I>> mapping) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.initialCall = initialCall;
        this.initialOutput = initialOutput;
        this.procedures = (Map<I, MealyMachine<S, SymbolWrapper<I>, T, O>>) procedures;
        this.mapping = mapping;
    }

    @Override
    public MealyTransition<StackSPMMState<S, SymbolWrapper<I>, T, O>, O> getTransition(StackSPMMState<S, SymbolWrapper<I>, T, O> state,
                                                                                       I i) {
        if (state.isSink() || state.isTerm()) {
            return sink();
        } else if (inputAlphabet.isInternalSymbol(i)) {
            if (state.isInit()) {
                return sink();
            }

            final SymbolWrapper<I> input = mapping.get(i);
            final MealyMachine<S, SymbolWrapper<I>, T, O> model = state.getProcedure();
            final T t = model.getTransition(state.getCurrentState(), input);

            if (t == null || outputAlphabet.isErrorSymbol(model.getTransitionOutput(t))) {
                return sink();
            }

            final S succ = model.getSuccessor(t);
            final StackSPMMState<S, SymbolWrapper<I>, T, O> next = state.updateState(succ);

            return new MealyTransition<>(next, model.getTransitionOutput(t));
        } else if (inputAlphabet.isCallSymbol(i)) {
            if (state.isInit() && !Objects.equals(this.initialCall, i)) {
                return sink();
            }

            final MealyMachine<S, SymbolWrapper<I>, T, O> model = this.procedures.get(i);

            if (model == null) {
                return sink();
            }

            final S next = model.getInitialState();

            if (next == null) {
                return sink();
            }

            // store the procedural successor in the stack so that we don't need to look it up on return symbols
            final StackSPMMState<S, SymbolWrapper<I>, T, O> returnState;
            final O output;
            if (state.isInit()) {
                returnState = StackSPMMState.term();
                output = initialOutput;
            } else {
                final SymbolWrapper<I> input = mapping.get(i);
                final MealyMachine<S, SymbolWrapper<I>, T, O> p = state.getProcedure();
                final T t = p.getTransition(state.getCurrentState(), input);

                if (t == null || outputAlphabet.isErrorSymbol(p.getTransitionOutput(t))) {
                    return sink();
                }
                returnState = state.updateState(p.getSuccessor(t));
                output = p.getTransitionOutput(t);
            }

            return new MealyTransition<>(returnState.push(model, next), output);
        } else if (inputAlphabet.isReturnSymbol(i)) {
            if (state.isInit()) {
                return sink();
            }

            // if we returned the state before, we checked that a procedure is available
            final MealyMachine<S, SymbolWrapper<I>, T, O> model = state.getProcedure();
            final T t = model.getTransition(state.getCurrentState(), mapping.get(i));

            if (t == null || outputAlphabet.isErrorSymbol(model.getTransitionOutput(t))) {
                return sink();
            }

            return new MealyTransition<>(state.pop(), model.getTransitionOutput(t));
        } else {
            return sink();
        }
    }

    @Override
    public StackSPMMState<S, SymbolWrapper<I>, T, O> getInitialState() {
        return StackSPMMState.init();
    }

    @Override
    public I getInitialProcedure() {
        return initialCall;
    }

    @Override
    public SPAAlphabet<I> getInputAlphabet() {
        return this.inputAlphabet;
    }

    @Override
    public SPAOutputAlphabet<O> getOutputAlphabet() {
        return this.outputAlphabet;
    }

    @Override
    public O getTransitionOutput(MealyTransition<StackSPMMState<S, SymbolWrapper<I>, T, O>, O> transition) {
        return transition.getOutput();
    }

    @Override
    public StackSPMMState<S, SymbolWrapper<I>, T, O> getSuccessor(MealyTransition<StackSPMMState<S, SymbolWrapper<I>, T, O>, O> transition) {
        return transition.getSuccessor();
    }

    private MealyTransition<StackSPMMState<S, SymbolWrapper<I>, T, O>, O> sink() {
        return new MealyTransition<>(StackSPMMState.sink(), outputAlphabet.getErrorSymbol());
    }

    @Override
    public Map<I, MealyMachine<?, I, ?, O>> getProcedures() {
        return Maps.transformValues(this.procedures, MealyView::new);
    }

    private class MealyView<S2, T2> implements MealyMachine<S2, I, T2, O> {

        private final MealyMachine<S2, SymbolWrapper<I>, T2, O> delegate;

        MealyView(MealyMachine<S2, SymbolWrapper<I>, T2, O> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Collection<S2> getStates() {
            return this.delegate.getStates();
        }

        @Override
        public O getTransitionOutput(T2 t2) {
            return this.delegate.getTransitionOutput(t2);
        }

        @Override
        public @Nullable T2 getTransition(S2 s2, I i) {
            final SymbolWrapper<I> w = mapping.get(i);
            if (w == null) {
                return null;
            }
            return delegate.getTransition(s2, w);
        }

        @Override
        public S2 getSuccessor(T2 t2) {
            return this.delegate.getSuccessor(t2);
        }

        @Override
        public @Nullable S2 getInitialState() {
            return this.delegate.getInitialState();
        }
    }

}