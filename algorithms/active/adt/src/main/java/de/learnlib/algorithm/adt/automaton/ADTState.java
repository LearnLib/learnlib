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
package de.learnlib.algorithm.adt.automaton;

import java.util.LinkedHashSet;
import java.util.Set;

import de.learnlib.AccessSequenceProvider;
import net.automatalib.automaton.base.AbstractFastState;
import net.automatalib.word.Word;

/**
 * Hypothesis state model.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ADTState<I, O> extends AbstractFastState<ADTTransition<I, O>>
        implements AccessSequenceProvider<I> {

    private final Set<ADTTransition<I, O>> incomingTransitions;

    private Word<I> accessSequence;

    public ADTState(int numInputs) {
        super(numInputs);
        incomingTransitions = new LinkedHashSet<>();
    }

    @Override
    public Word<I> getAccessSequence() {
        return accessSequence;
    }

    public void setAccessSequence(Word<I> accessSequence) {
        this.accessSequence = accessSequence;
    }

    @Override
    public void clearTransitionObject(ADTTransition<I, O> transition) {
        if (transition != null) {
            final ADTState<I, O> target = transition.getTarget();
            if (target != null) {
                target.getIncomingTransitions().remove(transition);
            }
        }
    }

    public Set<ADTTransition<I, O>> getIncomingTransitions() {
        return incomingTransitions;
    }
}
