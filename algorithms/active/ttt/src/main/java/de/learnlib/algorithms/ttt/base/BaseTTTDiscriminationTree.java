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
package de.learnlib.algorithms.ttt.base;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import de.learnlib.api.AccessSequenceProvider;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.discriminationtree.model.AbstractDiscriminationTree;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Word;

/**
 * The discrimination tree data structure.
 *
 * @param <I>
 *         input symbol type
 *
 * @author Malte Isberner
 */
public class BaseTTTDiscriminationTree<I, D>
        extends AbstractDiscriminationTree<Word<I>, I, D, TTTState<I, D>, AbstractBaseDTNode<I, D>> {

    public BaseTTTDiscriminationTree(MembershipOracle<I, D> oracle,
                                     Supplier<? extends AbstractBaseDTNode<I, D>> supplier) {
        this(oracle, supplier.get());
    }

    public BaseTTTDiscriminationTree(MembershipOracle<I, D> oracle, AbstractBaseDTNode<I, D> root) {
        super(root, oracle);
    }

    /**
     * Sifts an access sequence provided by an object into the tree, starting at the root. This operation performs a
     * "hard" sift, i.e., it will not stop at temporary nodes.
     *
     * @param asp
     *         the object providing the access sequence
     *
     * @return the leaf resulting from the sift operation
     */
    public AbstractBaseDTNode<I, D> sift(AccessSequenceProvider<I> asp) {
        return sift(asp, true);
    }

    /**
     * Sifts an access sequence provided by an object into the tree, starting at the root. This can either be a "soft"
     * sift, which stops either at the leaf <b>or</b> at the first temporary node, or a "hard" sift, stopping only at a
     * leaf.
     *
     * @param asp
     *         the object providing the access sequence
     */
    public AbstractBaseDTNode<I, D> sift(AccessSequenceProvider<I> asp, boolean hard) {
        return sift(asp.getAccessSequence(), hard);
    }

    public AbstractBaseDTNode<I, D> sift(Word<I> word, boolean hard) {
        return sift(root, word, hard);
    }

    public AbstractBaseDTNode<I, D> sift(AbstractBaseDTNode<I, D> start, Word<I> word, boolean hard) {
        AbstractBaseDTNode<I, D> curr = start;

        while (!curr.isLeaf() && (hard || !curr.isTemp())) {
            D outcome = super.oracle.answerQuery(word, curr.getDiscriminator());
            curr = curr.child(outcome);
        }

        return curr;
    }

    public AbstractBaseDTNode<I, D> sift(AbstractBaseDTNode<I, D> start, AccessSequenceProvider<I> asp, boolean hard) {
        return sift(start, asp.getAccessSequence(), hard);
    }

    @Override
    public AbstractBaseDTNode<I, D> sift(AbstractBaseDTNode<I, D> start, Word<I> prefix) {
        return sift(start, prefix, true);
    }

    @Override
    public VisualizationHelper<AbstractBaseDTNode<I, D>, Entry<D, AbstractBaseDTNode<I, D>>> getVisualizationHelper() {
        return new VisualizationHelper<AbstractBaseDTNode<I, D>, Entry<D, AbstractBaseDTNode<I, D>>>() {

            @Override
            public boolean getNodeProperties(AbstractBaseDTNode<I, D> node, Map<String, String> properties) {
                if (node.isLeaf()) {
                    properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
                    properties.put(NodeAttrs.LABEL, String.valueOf(node.getData()));
                } else {
                    properties.put(NodeAttrs.LABEL, node.getDiscriminator().toString());
                    if (!node.isTemp()) {
                        properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
                    } else if (node.isBlockRoot()) {
                        properties.put(NodeAttrs.SHAPE, NodeShapes.DOUBLEOCTAGON);
                    } else {
                        properties.put(NodeAttrs.SHAPE, NodeShapes.OCTAGON);
                    }
                }

                return true;
            }

            @Override
            public boolean getEdgeProperties(AbstractBaseDTNode<I, D> src,
                                             Map.Entry<D, AbstractBaseDTNode<I, D>> edge,
                                             AbstractBaseDTNode<I, D> tgt,
                                             Map<String, String> properties) {
                properties.put(EdgeAttrs.LABEL, String.valueOf(edge.getValue().getParentOutcome()));

                return true;
            }
        };
    }

}
