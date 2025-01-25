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

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.adt.adt.ADTNode;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.base.AbstractFastMutableDet;
import net.automatalib.automaton.transducer.MutableMealyMachine;
import net.automatalib.word.Word;

/**
 * Hypothesis model.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ADTHypothesis<I, O> extends AbstractFastMutableDet<ADTState<I, O>, I, ADTTransition<I, O>, Void, O>
        implements MutableMealyMachine<ADTState<I, O>, I, ADTTransition<I, O>, O>, AccessSequenceTransformer<I> {

    public ADTHypothesis(Alphabet<I> alphabet) {
        super(alphabet);
    }

    @Override
    public ADTState<I, O> getSuccessor(ADTTransition<I, O> transition) {
        return transition.getTarget();
    }

    public ADTTransition<I, O> createOpenTransition(ADTState<I, O> source,
                                                    I input,
                                                    ADTNode<ADTState<I, O>, I, O> siftTarget) {
        ADTTransition<I, O> result = new ADTTransition<>();
        result.setSource(source);
        result.setInput(input);
        result.setSiftNode(siftTarget);

        this.setTransition(source, input, result);

        return result;
    }

    @Override
    public void setTransition(ADTState<I, O> state, I input, ADTTransition<I, O> transition) {
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
    protected ADTState<I, O> createState(Void property) {
        return new ADTState<>(inputAlphabet.size());
    }

    @Override
    public ADTTransition<I, O> createTransition(ADTState<I, O> successor, O properties) {
        ADTTransition<I, O> result = new ADTTransition<>();
        result.setTarget(successor);
        result.setOutput(properties);
        return result;
    }

    @Override
    public void setTransitionOutput(ADTTransition<I, O> transition, O output) {
        transition.setOutput(output);
    }

    @Override
    public O getTransitionOutput(ADTTransition<I, O> transition) {
        return transition.getOutput();
    }

    @Override
    public Word<I> transformAccessSequence(Word<I> word) {
        final ADTState<I, O> state = this.getState(word);
        assert state != null;
        return state.getAccessSequence();
    }

}
