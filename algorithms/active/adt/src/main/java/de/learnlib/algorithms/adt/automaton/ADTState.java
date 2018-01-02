/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.algorithms.adt.automaton;

import java.util.LinkedHashSet;
import java.util.Set;

import de.learnlib.api.AccessSequenceProvider;
import net.automatalib.automata.base.fast.AbstractFastDetState;
import net.automatalib.words.Word;

/**
 * Hypothesis state model.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public class ADTState<I, O> extends AbstractFastDetState<ADTState<I, O>, ADTTransition<I, O>>
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
    public void clearTransition(final ADTTransition<I, O> transition) {
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
