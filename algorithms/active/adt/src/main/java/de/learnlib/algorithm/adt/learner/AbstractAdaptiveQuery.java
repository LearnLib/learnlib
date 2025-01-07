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
import de.learnlib.algorithm.adt.util.ADTUtil;
import de.learnlib.query.AdaptiveQuery;

/**
 * Utility class to share common implementations.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
abstract class AbstractAdaptiveQuery<I, O> implements AdaptiveQuery<I, O> {

    protected ADTNode<ADTState<I, O>, I, O> currentADTNode;
    private O tempOut;

    AbstractAdaptiveQuery(ADTNode<ADTState<I, O>, I, O> currentADTNode) {
        this.currentADTNode = currentADTNode;
    }

    @Override
    public Response processOutput(O out) {

        final ADTNode<ADTState<I, O>, I, O> succ = currentADTNode.getChild(out);

        if (succ == null) {
            this.tempOut = out;
            return Response.FINISHED;
        } else if (ADTUtil.isResetNode(succ)) {
            this.currentADTNode = succ.getChild(null);
            resetProgress();
            return Response.RESET;
        } else if (ADTUtil.isSymbolNode(succ)) {
            this.currentADTNode = succ;
            return Response.SYMBOL;
        } else {
            this.currentADTNode = succ;
            return Response.FINISHED;
        }
    }

    protected abstract void resetProgress();

    boolean needsPostProcessing() {
        return this.tempOut != null;
    }

    ADTNode<ADTState<I, O>, I, O> getCurrentADTNode() {
        return currentADTNode;
    }

    O getTempOut() {
        return tempOut;
    }
}
