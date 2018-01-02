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

import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.words.Word;

/**
 * Convenient class for word-based discrimination trees that already binds certain generics.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 * @param <D>
 *         node data type
 *
 * @author frohme
 */
public abstract class AbstractWordBasedDiscriminationTree<I, O, D>
        extends AbstractDiscriminationTree<Word<I>, I, O, D, AbstractWordBasedDTNode<I, O, D>> {

    public AbstractWordBasedDiscriminationTree(AbstractWordBasedDTNode<I, O, D> root, MembershipOracle<I, O> oracle) {
        super(root, oracle);
    }

    public AbstractWordBasedDTNode<I, O, D> sift(AbstractWordBasedDTNode<I, O, D> start, Word<I> prefix) {
        AbstractWordBasedDTNode<I, O, D> curr = start;

        while (!curr.isLeaf()) {
            O out = super.oracle.answerQuery(prefix, curr.getDiscriminator());
            curr = curr.child(out);
        }

        return curr;
    }
}
