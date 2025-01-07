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
package de.learnlib.algorithm.procedural.sba;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.learnlib.algorithm.procedural.SymbolWrapper;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.common.util.HashUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

class MappingSBA<S, I> implements SBA<S, I> {

    private final ProceduralInputAlphabet<I> alphabet;
    private final Map<I, SymbolWrapper<I>> mapping;
    private final SBA<S, SymbolWrapper<I>> delegate;

    private final Map<I, DFA<?, I>> procedures;

    MappingSBA(ProceduralInputAlphabet<I> alphabet, Map<I, SymbolWrapper<I>> mapping, SBA<S, SymbolWrapper<I>> delegate) {
        this.alphabet = alphabet;
        this.mapping = mapping;
        this.delegate = delegate;

        final Map<SymbolWrapper<I>, DFA<?, SymbolWrapper<I>>> p = delegate.getProcedures();
        this.procedures = new HashMap<>(HashUtil.capacity(p.size()));

        for (Entry<SymbolWrapper<I>, DFA<?, SymbolWrapper<I>>> e : p.entrySet()) {
            procedures.put(e.getKey().getDelegate(), new DFAView<>(e.getValue()));
        }
    }

    @Override
    public @Nullable S getTransition(S state, I i) {
        final SymbolWrapper<I> w = this.mapping.get(i);
        return w == null ? null : this.delegate.getTransition(state, w);
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
    public ProceduralInputAlphabet<I> getInputAlphabet() {
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
            return w == null ? null : delegate.getTransition(s, w);
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
