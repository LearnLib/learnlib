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
package de.learnlib.algorithm.adt.learner;

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.automaton.ADTTransition;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.word.Word;

/**
 * Utility class to close transitions. This query simply tracks the current ADT node for the access sequence of the
 * given transition and sets its output if the respective input symbol is traversed.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
class ADTAdaptiveQuery<I, O> extends AbstractAdaptiveQuery<I, O> {

    private final ADTTransition<I, O> transition;
    private final Word<I> accessSequence;

    private int asIndex;

    ADTAdaptiveQuery(ADTTransition<I, O> transition, ADTNode<ADTState<I, O>, I, O> root) {
        super(root);
        this.transition = transition;
        this.accessSequence = transition.getSource().getAccessSequence();
        this.asIndex = 0;
    }

    @Override
    public I getInput() {
        if (this.asIndex <= this.accessSequence.length()) {

            if (asIndex == this.accessSequence.length()) {
                return transition.getInput();
            }

            return this.accessSequence.getSymbol(this.asIndex);
        } else {
            return super.currentADTNode.getSymbol();
        }
    }

    @Override
    public Response processOutput(O out) {
        if (this.asIndex <= this.accessSequence.length()) {
            if (this.asIndex == this.accessSequence.length()) {
                this.transition.setOutput(out);
            }

            // if the ADT only consists of a leaf, we just set the transition output
            if (ADTUtil.isLeafNode(super.currentADTNode)) {
                return Response.FINISHED;
            }

            asIndex++;
            return Response.SYMBOL;
        } else {
            return super.processOutput(out);
        }
    }

    @Override
    protected void resetProgress() {
        this.asIndex = 0;
    }

    ADTTransition<I, O> getTransition() {
        return transition;
    }

    Word<I> getAccessSequence() {
        return this.accessSequence;
    }
}


