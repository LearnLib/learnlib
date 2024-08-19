/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.adt.learner;

import java.util.ArrayDeque;
import java.util.Deque;

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.query.AdaptiveQuery;
import net.automatalib.word.Word;

class ADSAmbiguityQuery<I, O> implements AdaptiveQuery<I, O> {

    private ADTNode<ADTState<I, O>, I, O> currentADTNode;

    /**
     * The index of the access sequence of the transition. If equal to the length of the access sequence the actual
     * transition input symbol is the current symbol. If larger than the length of the access sequence, the symbol
     * should be fetched from the ADT nodes.
     */
    private int asIndex;
    private final Word<I> accessSequence;
    private final Deque<I> oneShotPrefix;

    private boolean inOneShot;
    private ADTNode<ADTState<I, O>, I, O> tempADTNode;
    private O tempOut;

    ADSAmbiguityQuery(Word<I> accessSequence, Word<I> oneShotPrefix, ADTNode<ADTState<I, O>, I, O> root) {
        this.currentADTNode = root;
        this.accessSequence = accessSequence;
        this.oneShotPrefix = new ArrayDeque<>(oneShotPrefix.asList());
        this.inOneShot = false;
        this.asIndex = 0;
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
            final ADTNode<ADTState<I, O>, I, O> succ = currentADTNode.getChild(out);

            if (succ == null) {
                this.tempOut = out;
                this.tempADTNode = currentADTNode;
                return Response.FINISHED;
            } else if (ADTUtil.isResetNode(succ)) {
                this.currentADTNode = succ.getChild(null);
                this.asIndex = 0;
                return Response.RESET;
            } else if (ADTUtil.isSymbolNode(succ)) {
                this.currentADTNode = succ;
                return Response.SYMBOL;
            } else {
                this.currentADTNode = succ;
                return Response.FINISHED;
            }
        }
    }

    boolean needsPostProcessing() {
        return this.tempOut != null && this.tempADTNode != null;
    }

    ADTNode<ADTState<I, O>, I, O> getCurrentADTNode() {
        return this.currentADTNode;
    }

    O getTempOut() {
        return this.tempOut;
    }
}


