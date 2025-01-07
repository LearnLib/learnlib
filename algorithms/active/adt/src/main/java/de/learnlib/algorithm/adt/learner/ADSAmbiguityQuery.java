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

import java.util.ArrayDeque;
import java.util.Deque;

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.automaton.ADTState;
import net.automatalib.word.Word;

/**
 * Utility class to resolve ADS ambiguities. This query simply tracks the current ADT node for the given inputs.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
class ADSAmbiguityQuery<I, O> extends AbstractAdaptiveQuery<I, O> {

    private final Word<I> accessSequence;
    private final Deque<I> oneShotPrefix;

    private int asIndex;
    private boolean inOneShot;

    ADSAmbiguityQuery(Word<I> accessSequence, Word<I> oneShotPrefix, ADTNode<ADTState<I, O>, I, O> root) {
        super(root);
        this.accessSequence = accessSequence;
        this.oneShotPrefix = new ArrayDeque<>(oneShotPrefix.asList());
        this.asIndex = 0;
        this.inOneShot = false;
    }

    @Override
    public I getInput() {
        if (this.asIndex < this.accessSequence.length()) {
            return this.accessSequence.getSymbol(this.asIndex);
        } else {
            this.inOneShot = !this.oneShotPrefix.isEmpty();
            if (this.inOneShot) {
                return oneShotPrefix.poll();
            } else {
                return this.currentADTNode.getSymbol();
            }
        }
    }

    @Override
    public Response processOutput(O out) {
        if (this.asIndex < this.accessSequence.length()) {
            asIndex++;
            return Response.SYMBOL;
        } else if (this.inOneShot) {
            return Response.SYMBOL;
        } else {
            return super.processOutput(out);
        }
    }

    @Override
    protected void resetProgress() {
        this.asIndex = 0;
    }
}


