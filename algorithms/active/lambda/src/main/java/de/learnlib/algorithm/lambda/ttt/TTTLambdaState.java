/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.algorithm.lambda.ttt;

import de.learnlib.algorithm.lambda.ttt.dt.AbstractDecisionTree;
import de.learnlib.algorithm.lambda.ttt.pt.PrefixTree;
import de.learnlib.algorithm.lambda.ttt.st.SuffixTrie;

public class TTTLambdaState<I, D> {

    public final SuffixTrie<I> strie;
    public final PrefixTree<I, D> ptree;
    public final AbstractDecisionTree<I, D> dtree;

    public TTTLambdaState(SuffixTrie<I> strie, PrefixTree<I, D> ptree, AbstractDecisionTree<I, D> dtree) {
        this.strie = strie;
        this.ptree = ptree;
        this.dtree = dtree;
    }
}
