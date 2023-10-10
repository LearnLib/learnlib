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
package de.learnlib.algorithm.aaar;

import java.util.Collection;
import java.util.function.Function;

import de.learnlib.algorithm.aaar.abstraction.AbstractAbstractionTree;
import net.automatalib.automaton.transducer.MealyMachine;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TranslatingMealyMachine<S, AI, CI, T, O> implements MealyMachine<S, CI, T, O> {

    private final MealyMachine<S, CI, T, O> delegate;
    private final Function<CI, ? extends AbstractAbstractionTree<AI, CI, ?>> treeFetcher;

    public TranslatingMealyMachine(MealyMachine<S, CI, T, O> delegate,
                                   Function<CI, ? extends AbstractAbstractionTree<AI, CI, ?>> treeFetcher) {
        this.delegate = delegate;
        this.treeFetcher = treeFetcher;
    }

    @Override
    public Collection<S> getStates() {
        return this.delegate.getStates();
    }

    @Override
    public O getTransitionOutput(T t) {
        return this.delegate.getTransitionOutput(t);
    }

    @Override
    public @Nullable T getTransition(S s, CI i) {
        final AbstractAbstractionTree<AI, CI, ?> tree = treeFetcher.apply(i);
        final AI ai = tree.getAbstractSymbol(i);
        final CI ci = tree.getRepresentative(ai);

        return this.delegate.getTransition(s, ci);
    }

    @Override
    public S getSuccessor(T t) {
        return this.delegate.getSuccessor(t);
    }

    @Override
    public @Nullable S getInitialState() {
        return this.delegate.getInitialState();
    }
}
