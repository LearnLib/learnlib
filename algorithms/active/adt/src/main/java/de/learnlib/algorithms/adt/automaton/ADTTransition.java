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

import java.io.Serializable;

import de.learnlib.algorithms.adt.adt.ADTNode;

/**
 * Hypothesis transition model.
 *
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
public class ADTTransition<I, O> implements Serializable {

    private ADTState<I, O> source;
    private ADTState<I, O> target;

    private I input;
    private O output;

    private ADTNode<ADTState<I, O>, I, O> siftNode;

    private boolean isSpanningTreeEdge;

    public ADTState<I, O> getSource() {
        return source;
    }

    public void setSource(ADTState<I, O> source) {
        this.source = source;
    }

    public ADTState<I, O> getTarget() {
        return target;
    }

    public void setTarget(ADTState<I, O> target) {
        if (this.target != null) {
            this.target.getIncomingTransitions().remove(this);
        }

        this.target = target;

        if (this.target != null) {
            this.target.getIncomingTransitions().add(this);
            this.siftNode = null; // prevent memory leak
        }
    }

    public I getInput() {
        return input;
    }

    public void setInput(I input) {
        this.input = input;
    }

    public O getOutput() {
        return output;
    }

    public void setOutput(O output) {
        this.output = output;
    }

    public ADTNode<ADTState<I, O>, I, O> getSiftNode() {
        return siftNode;
    }

    public void setSiftNode(ADTNode<ADTState<I, O>, I, O> siftNode) {
        this.siftNode = siftNode;
    }

    public boolean isSpanningTreeEdge() {
        return isSpanningTreeEdge;
    }

    public void setIsSpanningTreeEdge(boolean isSpanningTreeEdge) {
        this.isSpanningTreeEdge = isSpanningTreeEdge;
    }

    public boolean needsSifting() {
        return this.target == null || this.output == null;
    }
}
