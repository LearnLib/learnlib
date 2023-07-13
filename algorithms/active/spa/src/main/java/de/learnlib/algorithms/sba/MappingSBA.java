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
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.sba.SBA;
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
class MappingSBA<S, I> implements SBA<S, I>, SimpleDTS<S, I> {

    private final SPAAlphabet<I> alphabet;
    private final Mapping<I, SymbolWrapper<I>> mapping;
    private final SBA<S, SymbolWrapper<I>> delegate;

    private final Map<I, DFA<?, I>> procedures;

    MappingSBA(SPAAlphabet<I> alphabet, Mapping<I, SymbolWrapper<I>> mapping, SBA<S, SymbolWrapper<I>> delegate) {
        this.alphabet = alphabet;
        this.mapping = mapping;
        this.delegate = delegate;

        final Map<SymbolWrapper<I>, DFA<?, SymbolWrapper<I>>> p = delegate.getProcedures();
        this.procedures = Maps.newHashMapWithExpectedSize(p.size());

        for (Entry<SymbolWrapper<I>, DFA<?, SymbolWrapper<I>>> e : p.entrySet()) {
            procedures.put(e.getKey().getDelegate(), new DFAView<>(e.getValue()));
        }
    }

    @Override
    public S getTransition(S state, I i) {
        return this.delegate.getTransition(state, this.mapping.get(i));
    }

    @Override
    public boolean isAccepting(S state) {
        return this.delegate.isAccepting(state);
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
        return this.alphabet;
    }

    @Override
    public Map<I, DFA<?, I>> getProcedures() {
        return this.procedures;
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