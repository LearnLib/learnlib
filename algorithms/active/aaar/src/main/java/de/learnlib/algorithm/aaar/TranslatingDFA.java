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
package de.learnlib.algorithm.aaar;

import java.util.Collection;
import java.util.function.Function;

import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import net.automatalib.automaton.fsa.DFA;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TranslatingDFA<S, AI, CI> implements DFA<S, CI> {

    private final DFA<S, CI> delegate;
    private final Function<CI, ? extends AbstractAbstractionTree<AI, CI, ?>> treeFetcher;

    public TranslatingDFA(DFA<S, CI> delegate, Function<CI, ? extends AbstractAbstractionTree<AI, CI, ?>> treeFetcher) {
        this.delegate = delegate;
        this.treeFetcher = treeFetcher;
    }

    @Override
    public Collection<S> getStates() {
        return this.delegate.getStates();
    }

    @Override
    public @Nullable S getTransition(S s, CI i) {
        final AbstractAbstractionTree<AI, CI, ?> tree = treeFetcher.apply(i);
        final AI ai = tree.getAbstractSymbol(i);
        final CI ci = tree.getRepresentative(ai);

        return this.delegate.getTransition(s, ci);
    }

    @Override
    public boolean isAccepting(S s) {
        return this.delegate.isAccepting(s);
    }

    @Override
    public @Nullable S getInitialState() {
        return this.delegate.getInitialState();
    }
}
