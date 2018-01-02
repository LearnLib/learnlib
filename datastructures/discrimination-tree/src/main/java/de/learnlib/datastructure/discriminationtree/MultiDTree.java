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
package de.learnlib.datastructure.discriminationtree;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;

/**
 * Generic n-ary discrimination tree specialization.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         node data type
 *
 * @author Malte Isberner
 */
public class MultiDTree<I, O, D> extends AbstractWordBasedDiscriminationTree<I, O, D> {

    public MultiDTree(MembershipOracle<I, O> oracle) {
        this(null, oracle);
    }

    public MultiDTree(D rootData, MembershipOracle<I, O> oracle) {
        super(new MultiDTNode<>(rootData), oracle);
    }

}
