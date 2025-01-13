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
package de.learnlib.algorithm.adt.adt;

import java.util.Collections;
import java.util.Map;

/**
 * Reset node implementation.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ADTResetNode<S, I, O> implements ADTNode<S, I, O> {

    private final ADTNode<S, I, O> successor;
    private ADTNode<S, I, O> parent;

    public ADTResetNode(ADTNode<S, I, O> successor) {
        this.successor = successor;
    }

    @Override
    public I getSymbol() {
        throw new UnsupportedOperationException("Reset nodes do not have a symbol");
    }

    @Override
    public void setSymbol(I symbol) {
        throw new UnsupportedOperationException("Reset nodes do not have a symbol");
    }

    @Override
    public ADTNode<S, I, O> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(ADTNode<S, I, O> parent) {
        this.parent = parent;
    }

    @Override
    public Map<O, ADTNode<S, I, O>> getChildren() {
        return Collections.singletonMap(null, this.successor);
    }

    @Override
    public S getState() {
        throw new UnsupportedOperationException("Reset nodes cannot reference a hypothesis state");
    }

    @Override
    public void setState(S state) {
        throw new UnsupportedOperationException("Reset nodes cannot reference a hypothesis state");
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.RESET_NODE;
    }

    @Override
    public String toString() {
        return "reset";
    }
}
