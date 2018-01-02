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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.api.AccessSequenceTransformer;
import net.automatalib.automata.base.fast.AbstractFastMutableDet;
import net.automatalib.automata.transout.MutableMealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * Hypothesis model.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public class ADTHypothesis<I, O> extends AbstractFastMutableDet<ADTState<I, O>, I, ADTTransition<I, O>, Void, O>
        implements MutableMealyMachine<ADTState<I, O>, I, ADTTransition<I, O>, O>, AccessSequenceTransformer<I> {

    public ADTHypothesis(final Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Nonnull
    @Override
    public ADTState<I, O> getSuccessor(final ADTTransition<I, O> transition) {
        return transition.getTarget();
    }

    @Nonnull
    public ADTTransition<I, O> createOpenTransition(final ADTState<I, O> source,
                                                    final I input,
                                                    final ADTNode<ADTState<I, O>, I, O> siftTarget) {
        ADTTransition<I, O> result = new ADTTransition<>();
        result.setSource(source);
        result.setInput(input);
        result.setSiftNode(siftTarget);

        this.setTransition(source, input, result);

        return result;
    }

    @Override
    public void setTransition(final ADTState<I, O> state, I input, final ADTTransition<I, O> transition) {
        final ADTTransition<I, O> oldTrans = getTransition(state, input);

        if (oldTrans != null) {
            oldTrans.getTarget().getIncomingTransitions().remove(oldTrans);
        }

        super.setTransition(state, input, transition);

        if (transition != null) {
            transition.setSource(state);
            transition.setInput(input);
        }
    }

    @Override
    protected ADTState<I, O> createState(final Void property) {
        return new ADTState<>(inputAlphabet.size());
    }

    @Nonnull
    @Override
    public ADTTransition<I, O> createTransition(final ADTState<I, O> successor, @Nullable final O properties) {
        ADTTransition<I, O> result = new ADTTransition<>();
        result.setTarget(successor);
        result.setOutput(properties);
        return result;
    }

    @Override
    public void setTransitionOutput(final ADTTransition<I, O> transition, @Nullable final O output) {
        transition.setOutput(output);
    }

    @Nullable
    @Override
    public O getTransitionOutput(final ADTTransition<I, O> transition) {
        return transition.getOutput();
    }

    @Override
    public Word<I> transformAccessSequence(final Word<I> word) {
        return this.getState(word).getAccessSequence();
    }

    @Override
    public boolean isAccessSequence(final Word<I> word) {
        return this.getState(word).getAccessSequence().equals(word);
    }

}
