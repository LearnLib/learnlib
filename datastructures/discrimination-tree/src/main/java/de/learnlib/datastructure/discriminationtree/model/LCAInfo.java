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
package de.learnlib.datastructure.discriminationtree.model;

/**
 * DAO for aggregating information about the least common ancestor of two subtrees in a discrimination tree.
 *
 * @param <O>
 *         output symbol type
 * @param <N>
 *         node type
 *
 * @author Malte Isberner
 */
public class LCAInfo<O, N> {

    public final N leastCommonAncestor;

    public final O subtree1Label;

    public final O subtree2Label;

    public LCAInfo(N leastCommonAncestor, O subtree1Label, O subtree2Label) {
        this(leastCommonAncestor, subtree1Label, subtree2Label, false);
    }

    LCAInfo(N leastCommonAncestor, O subtree1Label, O subtree2Label, boolean swap) {
        this.leastCommonAncestor = leastCommonAncestor;
        if (swap) {
            this.subtree1Label = subtree2Label;
            this.subtree2Label = subtree1Label;
        } else {
            this.subtree1Label = subtree1Label;
            this.subtree2Label = subtree2Label;
        }
    }
}
