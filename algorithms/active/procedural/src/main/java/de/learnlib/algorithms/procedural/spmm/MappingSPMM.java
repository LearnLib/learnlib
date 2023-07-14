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
package de.learnlib.algorithms.procedural.spmm;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import de.learnlib.algorithms.procedural.SymbolWrapper;
import net.automatalib.automata.spmm.SPMM;
import net.automatalib.automata.transducers.MealyMachine;
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
class MappingSPMM<S, I, T, O> implements SPMM<S, I, T, O> {

    private final SPAAlphabet<I> inputAlphabet;
    private final SPAOutputAlphabet<O> outputAlphabet;
    private final Mapping<I, SymbolWrapper<I>> mapping;
    private final SPMM<S, SymbolWrapper<I>, T, O> delegate;

    private final Map<I, MealyMachine<?, I, ?, O>> procedures;

    MappingSPMM(SPAAlphabet<I> inputAlphabet,
                SPAOutputAlphabet<O> outputAlphabet,
                Mapping<I, SymbolWrapper<I>> mapping,
                SPMM<S, SymbolWrapper<I>, T, O> delegate) {
        this.inputAlphabet = inputAlphabet;
        this.outputAlphabet = outputAlphabet;
        this.mapping = mapping;
        this.delegate = delegate;

        final Map<SymbolWrapper<I>, MealyMachine<?, SymbolWrapper<I>, ?, O>> p = delegate.getProcedures();
        this.procedures = Maps.newHashMapWithExpectedSize(p.size());

        for (Entry<SymbolWrapper<I>, MealyMachine<?, SymbolWrapper<I>, ?, O>> e : p.entrySet()) {
            procedures.put(e.getKey().getDelegate(), new MealyView<>(e.getValue()));
        }
    }

    @Override
    public T getTransition(S state, I i) {
        return this.delegate.getTransition(state, this.mapping.get(i));
    }

    @Override
    public S getInitialState() {
        return this.delegate.getInitialState();
    }

    @Override
    public @Nullable I getInitialProcedure() {
        final SymbolWrapper<I> init = this.delegate.getInitialProcedure();
        return init == null ? null : init.getDelegate();
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
    public O getTransitionOutput(T transition) {
        return this.delegate.getTransitionOutput(transition);
    }

    @Override
    public S getSuccessor(T transition) {
        return this.delegate.getSuccessor(transition);
    }

    @Override
    public Map<I, MealyMachine<?, I, ?, O>> getProcedures() {
        return this.procedures;
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