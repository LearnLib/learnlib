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
package de.learnlib.algorithms.discriminationtree.hypothesis.vpda;

import de.learnlib.api.AccessSequenceProvider;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.discriminationtree.model.AbstractDiscriminationTree;
import net.automatalib.words.Word;

/**
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class DTree<I> extends AbstractDiscriminationTree<ContextPair<I>, I, Boolean, HypLoc<I>, DTNode<I>> {

    public DTree(MembershipOracle<I, Boolean> oracle) {
        super(new DTNode<>(null, false), oracle);
    }

    public DTree(DTNode<I> root, MembershipOracle<I, Boolean> oracle) {
        super(root, oracle);
    }

    @Override
    public DTNode<I> sift(DTNode<I> start, Word<I> prefix) {
        return sift(start, prefix, true);
    }

    public DTNode<I> sift(DTNode<I> start, Word<I> as, boolean hard) {
        DTNode<I> curr = start;
        while (curr.isInner() && (hard || !curr.isTemp())) {
            ContextPair<I> discr = curr.getDiscriminator();
            Word<I> prefix = discr.getPrefix().concat(as);
            Boolean outcome = oracle.answerQuery(prefix, discr.getSuffix());

            curr = curr.getChild(outcome);
        }

        return curr;
    }

    public DTNode<I> sift(AccessSequenceProvider<I> asp) {
        return sift(getRoot(), asp, false);
    }

    public DTNode<I> sift(DTNode<I> start, AccessSequenceProvider<I> asp, boolean hard) {
        return sift(start, asp.getAccessSequence(), hard);
    }

}
