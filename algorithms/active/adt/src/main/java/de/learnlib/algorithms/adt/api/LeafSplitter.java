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
package de.learnlib.algorithms.adt.api;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADTNode;
import net.automatalib.words.Word;

/**
 * Interface for configuration objects that specify how to split the ADT leaf of a hypothesis state that needs
 * refinement.
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public interface LeafSplitter {

    /**
     * Split the specified node to correctly distinguish between the old and new hypothesis state.
     *
     * @param nodeToSplit
     *         the existing leaf that should be split
     * @param distinguishingSuffix
     *         the input sequence that splits the hypothesis state of the leaf to split and the new node.
     * @param oldOutput
     *         the hypothesis output of the node to split given the distinguishing suffix
     * @param newOutput
     *         the hypothesis output of the new leaf given the distinguishing suffix
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return the new leaf that should reference the new hypothesis state
     */
    <S, I, O> ADTNode<S, I, O> split(ADTNode<S, I, O> nodeToSplit,
                                     Word<I> distinguishingSuffix,
                                     Word<O> oldOutput,
                                     Word<O> newOutput);
}
