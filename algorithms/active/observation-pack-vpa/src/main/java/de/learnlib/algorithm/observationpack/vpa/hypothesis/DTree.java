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
package de.learnlib.algorithm.observationpack.vpa.hypothesis;

import java.util.List;
import java.util.function.Predicate;

import de.learnlib.datastructure.discriminationtree.model.AbstractDiscriminationTree;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.query.DefaultQuery;
import net.automatalib.word.Word;

/**
 * Specific discrimination tree implementation.
 *
 * @param <I>
 *         input symbol type
 */
public class DTree<I> extends AbstractDiscriminationTree<ContextPair<I>, I, Boolean, HypLoc<I>, DTNode<I>> {

    public DTree(MembershipOracle<I, Boolean> oracle) {
        super(new DTNode<>(null, false), oracle);
    }

    @Override
    public DTNode<I> sift(DTNode<I> start, Word<I> prefix) {
        return sift(start, prefix, true);
    }

    @Override
    public List<DTNode<I>> sift(List<DTNode<I>> starts, List<Word<I>> prefixes) {
        return sift(starts, prefixes, true);
    }

    public DTNode<I> sift(DTNode<I> start, Word<I> as, boolean hard) {
        return super.sift(start, as, getSiftPredicate(hard));
    }

    public List<DTNode<I>> sift(List<DTNode<I>> starts, List<Word<I>> prefixes, boolean hard) {
        return super.sift(starts, prefixes, getSiftPredicate(hard));
    }

    @Override
    protected DefaultQuery<I, Boolean> buildQuery(DTNode<I> node, Word<I> prefix) {
        final ContextPair<I> discr = node.getDiscriminator();
        final Word<I> completePrefix = discr.getPrefix().concat(prefix);
        return new DefaultQuery<>(completePrefix, node.getDiscriminator().getSuffix());
    }

    private static <I> Predicate<DTNode<I>> getSiftPredicate(boolean hard) {
        return n -> n.isInner() && (hard || !n.isTemp());
    }

}
