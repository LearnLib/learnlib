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
package de.learnlib.algorithms.adt.adt;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.oracle.SymbolQueryOracle;
import net.automatalib.graphs.ads.impl.AbstractRecursiveADSLeafNode;
import net.automatalib.words.Word;

/**
 * Leaf node implementation.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ADTLeafNode<S, I, O> extends AbstractRecursiveADSLeafNode<S, I, O, ADTNode<S, I, O>>
        implements ADTNode<S, I, O> {

    public ADTLeafNode(@Nullable ADTNode<S, I, O> parent, @Nullable S hypothesisState) {
        super(parent, hypothesisState);
    }

    @Override
    public ADTNode<S, I, O> sift(SymbolQueryOracle<I, O> oracle, final Word<I> prefix) {
        throw new UnsupportedOperationException("Final nodes cannot sift words");
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.LEAF_NODE;
    }
}
