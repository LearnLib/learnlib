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
package de.learnlib.algorithms.adt.model;

import java.util.Collections;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADT;
import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.SubtreeReplacer;

/**
 * A class that describes the possible result a {@link SubtreeReplacer} can return. A replacement may either be <ul>
 * <li> a complete replacement </li> <li> a partial replacement, that additionally defines which hypothesis states are
 * not covered by the replacement </li> </ul>
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
public class ReplacementResult<S, I, O> {

    private final ADTNode<S, I, O> nodeToReplace, replacement;

    private final Set<S> cutoutNodes;

    public ReplacementResult(final ADTNode<S, I, O> nodeToReplace, final ADTNode<S, I, O> replacement) {
        this(nodeToReplace, replacement, Collections.emptySet());
    }

    public ReplacementResult(final ADTNode<S, I, O> nodeToReplace,
                             final ADTNode<S, I, O> replacement,
                             final Set<S> cutoutNodes) {
        this.nodeToReplace = nodeToReplace;
        this.replacement = replacement;
        this.cutoutNodes = cutoutNodes;
    }

    /**
     * The {@link ADT} subtree (root-node) that forms the replacement.
     *
     * @return the replacement
     */
    public ADTNode<S, I, O> getReplacement() {
        return replacement;
    }

    /**
     * The {@link ADT} subtree (root-node) that should be replaced.
     *
     * @return the node to replace
     */
    public ADTNode<S, I, O> getNodeToReplace() {
        return nodeToReplace;
    }

    /**
     * The set of hypothesis states that are not covered on the proposed replacement.
     *
     * @return the set
     */
    public Set<S> getCutoutNodes() {
        return cutoutNodes;
    }
}
