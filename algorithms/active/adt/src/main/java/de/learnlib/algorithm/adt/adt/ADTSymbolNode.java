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
package de.learnlib.algorithm.adt.adt;

import de.learnlib.oracle.SymbolQueryOracle;
import net.automatalib.graph.ads.AbstractRecursiveADSSymbolNode;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Symbol node implementation.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ADTSymbolNode<S, I, O> extends AbstractRecursiveADSSymbolNode<S, I, O, ADTNode<S, I, O>>
        implements ADTNode<S, I, O> {

    public ADTSymbolNode(@Nullable ADTNode<S, I, O> parent, I symbol) {
        super(parent, symbol);
    }

    @Override
    public ADTNode<S, I, O> sift(SymbolQueryOracle<I, O> oracle, Word<I> prefix) {
        final O o = oracle.query(super.getSymbol());

        final ADTNode<S, I, O> successor = super.getChildren().get(o);

        if (successor == null) {
            final ADTNode<S, I, O> result = new ADTLeafNode<>(this, null);
            super.getChildren().put(o, result);
            return result;
        }

        return successor;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.SYMBOL_NODE;
    }

    @Override
    public String toString() {
        return String.valueOf(super.getSymbol());
    }

}
