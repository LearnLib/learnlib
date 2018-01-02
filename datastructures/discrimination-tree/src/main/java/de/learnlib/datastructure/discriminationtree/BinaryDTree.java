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
 * Binary discrimination tree specialization.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         node data type
 *
 * @author Malte Isberner
 */
public class BinaryDTree<I, D> extends AbstractWordBasedDiscriminationTree<I, Boolean, D> {

    public BinaryDTree(MembershipOracle<I, Boolean> oracle) {
        this(null, oracle);
    }

    public BinaryDTree(D rootData, MembershipOracle<I, Boolean> oracle) {
        super(new BinaryDTNode<>(rootData), oracle);
    }

}
