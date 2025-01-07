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
package de.learnlib.datastructure.discriminationtree;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.word.Word;

/**
 * Binary discrimination tree specialization.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         node data type
 */
public class BinaryDTree<I, D> extends AbstractWordBasedDiscriminationTree<I, Boolean, D> {

    public BinaryDTree(MembershipOracle<I, Boolean> oracle) {
        this(oracle, false);
    }

    public BinaryDTree(MembershipOracle<I, Boolean> oracle, boolean epsilonRoot) {
        this(new BinaryDTNode<>(null), oracle, epsilonRoot);
    }

    public BinaryDTree(BinaryDTNode<I, D> root, MembershipOracle<I, Boolean> oracle, boolean epsilonRoot) {
        super(root, oracle);
        if (epsilonRoot) {
            root.split(Word.epsilon(), false, true);
        }
    }

}
