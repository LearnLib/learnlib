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
package de.learnlib.algorithm.aaar.abstraction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import de.learnlib.algorithm.aaar.Abstraction;
import de.learnlib.algorithm.aaar.abstraction.Node.InnerNode;
import de.learnlib.algorithm.aaar.abstraction.Node.Leaf;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.graph.Graph;
import net.automatalib.graph.concept.GraphViewable;
import net.automatalib.visualization.DefaultVisualizationHelper;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.word.Word;

public abstract class AbstractAbstractionTree<AI, CI, D>
        implements Abstraction<AI, CI>, GraphViewable, Graph<Node, Node> {

    private Node root;

    private final MembershipOracle<CI, D> oracle;

    private final Map<AI, CI> gamma;

    public AbstractAbstractionTree(AI rootA, CI rootC, MembershipOracle<CI, D> o) {
        this.root = new Leaf<>(rootA, rootC);
        this.oracle = o;

        this.gamma = new HashMap<>();
        this.gamma.put(rootA, rootC);
    }

    public AI splitLeaf(CI repOld, CI repNew, Word<CI> prefix, Word<CI> suffix, D outOld) {

        final Leaf<AI, CI> l = new Leaf<>(createAbstractionForRepresentative(repNew), repNew);
        gamma.put(l.abs, repNew);

        Node cur = root;
        Node parent = null;

        while (cur instanceof InnerNode) {
            @SuppressWarnings("unchecked")
            final InnerNode<CI, D> n = (InnerNode<CI, D>) cur;
            final Word<CI> test = n.prefix.append(repOld).concat(n.suffix);
            final D out = oracle.answerQuery(test);

            parent = cur;
            if (Objects.equals(n.out, out)) {
                cur = n.equalsNext;
            } else {
                cur = n.otherNext;
            }

        }

        final InnerNode<CI, D> newNode = new InnerNode<>(prefix, suffix, outOld, cur, l);

        if (parent == null) {
            root = newNode;
        } else {
            final InnerNode<?, ?> parentAsInner = (InnerNode<?, ?>) parent;
            if (parentAsInner.equalsNext == cur) {
                parentAsInner.equalsNext = newNode;
            } else {
                parentAsInner.otherNext = newNode;
            }
        }

        return l.abs;
    }

    @Override
    public AI getAbstractSymbol(CI c) {
        Node cur = root;

        while (cur instanceof InnerNode) {
            @SuppressWarnings("unchecked")
            final InnerNode<CI, D> n = (InnerNode<CI, D>) cur;
            final Word<CI> test = n.prefix.append(c).concat(n.suffix);
            final D out = oracle.answerQuery(test);

            if (Objects.equals(n.out, out)) {
                cur = n.equalsNext;
            } else {
                cur = n.otherNext;
            }
        }

        @SuppressWarnings("unchecked")
        final Leaf<AI, CI> leaf = (Leaf<AI, CI>) cur;
        return leaf.abs;
    }

    @Override
    public CI getRepresentative(AI a) {
        final CI ci = gamma.get(a);
        assert ci != null;
        return ci;
    }

    public Collection<CI> getRepresentativeSymbols() {
        return Collections.unmodifiableCollection(this.gamma.values());
    }

    @Override
    public Graph<?, ?> graphView() {
        return this;
    }

    @Override
    public Collection<Node> getOutgoingEdges(Node node) {
        if (node instanceof InnerNode) {
            final InnerNode<?, ?> n = (InnerNode<?, ?>) node;
            return Arrays.asList(n.equalsNext, n.otherNext);
        }

        return Collections.emptySet();
    }

    @Override
    public Node getTarget(Node edge) {
        return edge;
    }

    @Override
    public Collection<Node> getNodes() {

        final List<Node> result = new ArrayList<>(this.gamma.size());

        final Queue<Node> nodes = new ArrayDeque<>();
        nodes.add(root);

        while (!nodes.isEmpty()) {
            final Node n = nodes.poll();
            if (n instanceof InnerNode) {
                final InnerNode<?, ?> in = (InnerNode<?, ?>) n;
                result.add(in);
                nodes.add(in.equalsNext);
                nodes.add(in.otherNext);
            } else {
                assert n != null;
                result.add(n);
            }
        }

        return result;
    }

    @Override
    public VisualizationHelper<Node, Node> getVisualizationHelper() {
        return new DefaultVisualizationHelper<Node, Node>() {

            @Override
            public boolean getNodeProperties(Node node, Map<String, String> properties) {
                super.getNodeProperties(node, properties);

                if (node instanceof InnerNode) {
                    final InnerNode<?, ?> n = (InnerNode<?, ?>) node;
                    properties.put(NodeAttrs.LABEL, n.prefix + ", " + n.suffix);
                } else if (node instanceof Leaf) {
                    final Leaf<?, ?> l = (Leaf<?, ?>) node;
                    properties.put(NodeAttrs.LABEL, String.format("Abs.: '%s'%nRep.: '%s'", l.abs, l.rep));
                }

                return true;
            }

            @Override
            public boolean getEdgeProperties(Node src, Node edge, Node tgt, Map<String, String> properties) {
                super.getEdgeProperties(src, edge, tgt, properties);

                if (src instanceof InnerNode) {
                    final InnerNode<?, ?> n = (InnerNode<?, ?>) src;
                    if (n.equalsNext == tgt) {
                        properties.put(EdgeAttrs.LABEL, "== " + n.out);
                    } else {
                        properties.put(EdgeAttrs.LABEL, "!= " + n.out);
                        properties.put(EdgeAttrs.STYLE, EdgeStyles.DASHED);
                    }
                }

                return true;
            }
        };
    }

    protected abstract AI createAbstractionForRepresentative(CI ci);
}
