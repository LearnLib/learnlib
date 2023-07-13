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
package de.learnlib.algorithms.sba;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.sba.SBA;
import net.automatalib.automata.spa.StackSPAState;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.ts.simple.SimpleDTS;
import net.automatalib.words.SPAAlphabet;
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
public class MappedStackSBA<S, I>
        implements SBA<StackSPAState<SymbolWrapper<I>, S>, I>, SimpleDTS<StackSPAState<SymbolWrapper<I>, S>, I> {

    private final SPAAlphabet<I> alphabet;
    private final I initialCall;
    private final Map<I, DFA<S, SymbolWrapper<I>>> procedures;
    private final Mapping<I, SymbolWrapper<I>> mapping;

    // cast is fine, because we make sure to only query states belonging to the respective procedures
    @SuppressWarnings("unchecked")
    public MappedStackSBA(SPAAlphabet<I> alphabet,
                          I initialCall,
                          Map<I, ? extends DFA<? extends S, SymbolWrapper<I>>> procedures,
                          Mapping<I, SymbolWrapper<I>> mapping) {
        this.alphabet = alphabet;
        this.initialCall = initialCall;
        this.procedures = (Map<I, DFA<S, SymbolWrapper<I>>>) procedures;
        this.mapping = mapping;
    }

    @Override
    public StackSPAState<SymbolWrapper<I>, S> getTransition(StackSPAState<SymbolWrapper<I>, S> state, I i) {
        if (state.isSink() || state.isTerm()) {
            return StackSPAState.sink();
        } else if (alphabet.isInternalSymbol(i)) {
            if (state.isInit()) {
                return StackSPAState.sink();
            }

            final SymbolWrapper<I> input = mapping.get(i);
            final DFA<S, SymbolWrapper<I>> model = state.getProcedure();
            final S next = model.getTransition(state.getCurrentState(), input);

            // undefined internal transition
            if (next == null || !model.isAccepting(next)) {
                return StackSPAState.sink();
            }

            return state.updateState(next);
        } else if (alphabet.isCallSymbol(i)) {
            if (state.isInit() && !Objects.equals(this.initialCall, i)) {
                return StackSPAState.sink();
            }

            final DFA<S, SymbolWrapper<I>> model = this.procedures.get(i);

            if (model == null) {
                return StackSPAState.sink();
            }

            final S next = model.getInitialState();

            if (next == null) {
                return StackSPAState.sink();
            }

            // store the procedural successor in the stack so that we don't need to look it up on return symbols
            final StackSPAState<SymbolWrapper<I>, S> returnState;
            if (state.isInit()) {
                returnState = StackSPAState.term();
            } else {
                final SymbolWrapper<I> input = mapping.get(i);
                final DFA<S, SymbolWrapper<I>> p = state.getProcedure();
                final S succ = p.getSuccessor(state.getCurrentState(), input);
                if (succ == null || !p.isAccepting(succ)) {
                    return StackSPAState.sink();
                }
                returnState = state.updateState(succ);
            }

            return returnState.push(model, next);
        } else if (alphabet.isReturnSymbol(i)) {
            if (state.isInit()) {
                return StackSPAState.sink();
            }

            // if we returned the state before, we checked that a procedure is available
            final DFA<S, SymbolWrapper<I>> model = state.getProcedure();
            final S succ = model.getSuccessor(state.getCurrentState(), mapping.get(i));

            // cannot return, reject word
            if (succ == null || !model.isAccepting(succ)) {
                return StackSPAState.sink();
            }

            return state.pop();
        } else {
            return StackSPAState.sink();
        }
    }

    @Override
    public boolean isAccepting(StackSPAState<SymbolWrapper<I>, S> state) {
        return !state.isSink() &&
               (state.isInit() || state.isTerm() || state.getProcedure().isAccepting(state.getCurrentState()));
    }

    @Override
    public StackSPAState<SymbolWrapper<I>, S> getInitialState() {
        return StackSPAState.init();
    }

    @Override
    public I getInitialProcedure() {
        return initialCall;
    }

    @Override
    public SPAAlphabet<I> getInputAlphabet() {
        return this.alphabet;
    }

    @Override
    public Map<I, DFA<?, I>> getProcedures() {
        return Maps.transformValues(this.procedures, DFAView::new);
    }

    private class DFAView<S2> implements DFA<S2, I> {

        private final DFA<S2, SymbolWrapper<I>> delegate;

        DFAView(DFA<S2, SymbolWrapper<I>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Collection<S2> getStates() {
            return delegate.getStates();
        }

        @Override
        public @Nullable S2 getTransition(S2 s, I i) {
            final SymbolWrapper<I> w = mapping.get(i);
            if (w == null) {
                return null;
            }
            return delegate.getTransition(s, w);
        }

        @Override
        public boolean isAccepting(S2 s) {
            return delegate.isAccepting(s);
        }

        @Override
        public @Nullable S2 getInitialState() {
            return delegate.getInitialState();
        }
    }
}