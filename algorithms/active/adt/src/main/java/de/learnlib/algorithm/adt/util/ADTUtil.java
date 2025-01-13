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
package de.learnlib.algorithm.adt.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import de.learnlib.algorithm.adt.adt.ADTLeafNode;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.adt.ADTNode.NodeType;
import de.learnlib.algorithm.adt.adt.ADTSymbolNode;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.graph.ads.ADSNode;
import net.automatalib.util.automaton.ads.ADSUtil;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility class, that offers some operations revolving around adaptive distinguishing sequences.
 */
public final class ADTUtil {

    private ADTUtil() {
        // prevent instantiation
    }

    public static boolean isLeafNode(@Nullable ADTNode<?, ?, ?> node) {
        return checkNodeType(node, ADTNode.NodeType.LEAF_NODE);
    }

    public static boolean isResetNode(@Nullable ADTNode<?, ?, ?> node) {
        return checkNodeType(node, ADTNode.NodeType.RESET_NODE);
    }

    public static boolean isSymbolNode(@Nullable ADTNode<?, ?, ?> node) {
        return checkNodeType(node, ADTNode.NodeType.SYMBOL_NODE);
    }

    private static boolean checkNodeType(@Nullable ADTNode<?, ?, ?> node, ADTNode.NodeType type) {
        return node != null && node.getNodeType() == type;
    }

    public static <S, I, O> ADTNode<S, I, O> getStartOfADS(ADTNode<S, I, O> node) {

        ADTNode<S, I, O> result = node;
        ADTNode<S, I, O> iter = node.getParent();

        while (iter != null && !ADTUtil.isResetNode(iter)) {
            result = iter;
            iter = iter.getParent();
        }

        return result;
    }

    public static <S> Set<S> collectHypothesisStates(ADTNode<S, ?, ?> root) {
        final Set<S> result = new LinkedHashSet<>();
        collectHypothesisStatesRecursively(result, root);
        return result;
    }

    private static <S> void collectHypothesisStatesRecursively(Set<S> nodes, ADTNode<S, ?, ?> current) {
        if (ADTUtil.isLeafNode(current)) {
            nodes.add(current.getState());
        } else {
            for (ADTNode<S, ?, ?> n : current.getChildren().values()) {
                collectHypothesisStatesRecursively(nodes, n);
            }
        }
    }

    public static <S, I, O> Set<ADTNode<S, I, O>> collectLeaves(ADTNode<S, I, O> root) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        collectNodesRecursively(result, root, NodeType.LEAF_NODE);
        return result;
    }

    public static <S, I, O> Set<ADTNode<S, I, O>> collectResetNodes(ADTNode<S, I, O> root) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        collectNodesRecursively(result, root, NodeType.RESET_NODE);
        return result;
    }

    private static <S, I, O> void collectNodesRecursively(Set<ADTNode<S, I, O>> nodes,
                                                          ADTNode<S, I, O> current,
                                                          NodeType type) {
        if (current.getNodeType() == type) {
            nodes.add(current);
        }

        for (ADTNode<S, I, O> n : current.getChildren().values()) {
            collectNodesRecursively(nodes, n, type);
        }
    }

    public static <S, I, O> Set<ADTNode<S, I, O>> collectADSNodes(ADTNode<S, I, O> root, boolean includeRoot) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        if (includeRoot) {
            result.add(root);
        }
        collectADSNodesRecursively(result, root);
        return result;
    }

    private static <S, I, O> void collectADSNodesRecursively(Set<ADTNode<S, I, O>> nodes,
                                                             ADTNode<S, I, O> current) {
        if (ADTUtil.isResetNode(current)) {
            nodes.addAll(current.getChildren().values());
        }

        for (ADTNode<S, I, O> n : current.getChildren().values()) {
            collectADSNodesRecursively(nodes, n);
        }
    }

    public static <S, I, O> Set<ADTNode<S, I, O>> collectDirectSubADSs(ADTNode<S, I, O> node) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        collectDirectSubTreesRecursively(result, node);
        return result;
    }

    private static <S, I, O> void collectDirectSubTreesRecursively(Set<ADTNode<S, I, O>> nodes,
                                                                   ADTNode<S, I, O> current) {
        if (ADTUtil.isResetNode(current)) {
            nodes.addAll(current.getChildren().values());
        } else {
            for (ADTNode<S, I, O> n : current.getChildren().values()) {
                collectDirectSubTreesRecursively(nodes, n);
            }
        }
    }

    public static <S, I, O> Pair<Word<I>, Word<O>> buildTraceForNode(ADTNode<S, I, O> node) {
        final Predicate<ADTNode<S, I, O>> predicate = ADTUtil::isResetNode;
        return ADSUtil.buildTraceForNode(node, predicate.negate());
    }

    public static <S, I, O> O getOutputForSuccessor(ADTNode<S, I, O> node, ADTNode<S, I, O> successor) {
        return ADSUtil.getOutputForSuccessor(node, successor);
    }

    /**
     * Utility method that wraps an ADS of type {@link ADSNode} into the equivalent ADS of type {@link ADTNode}.
     *
     * @param node
     *         the root node of the ADS
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return an equivalent ADS using the {@link ADTNode} interface
     */
    public static <S, I, O> ADTNode<S, I, O> buildFromADS(ADSNode<S, I, O> node) {

        if (node.isLeaf()) {
            return new ADTLeafNode<>(null, node.getState());
        }

        final ADTNode<S, I, O> result = new ADTSymbolNode<>(null, node.getSymbol());

        for (Map.Entry<O, ADSNode<S, I, O>> entry : node.getChildren().entrySet()) {
            final O adsOutput = entry.getKey();
            final ADSNode<S, I, O> adsNode = entry.getValue();

            final ADTNode<S, I, O> newChild = buildFromADS(adsNode);
            newChild.setParent(result);
            result.getChildren().put(adsOutput, newChild);
        }

        return result;
    }

    /**
     * Computes how often reset nodes are encountered when traversing from the given node to the leaves of the induced
     * subtree of the given node.
     *
     * @param adt
     *         the node whose subtree should be analyzed
     *
     * @return the number of encountered reset nodes
     */
    public static int computeEffectiveResets(ADTNode<?, ?, ?> adt) {
        return computeEffectiveResetsInternal(adt, 0);
    }

    private static int computeEffectiveResetsInternal(ADTNode<?, ?, ?> ads, int accumulatedResets) {
        if (ADTUtil.isLeafNode(ads)) {
            return accumulatedResets;
        }

        final int nextCosts = ADTUtil.isResetNode(ads) ? accumulatedResets + 1 : accumulatedResets;

        int resets = 0;

        for (ADTNode<?, ?, ?> value : ads.getChildren().values()) {
            resets += computeEffectiveResetsInternal(value, nextCosts);
        }

        return resets;
    }

    public static <S, I, O> Pair<ADTNode<S, I, O>, ADTNode<S, I, O>> buildADSFromTrace(MealyMachine<S, I, ?, O> automaton,
                                                                                       Word<I> trace,
                                                                                       S state) {
        return ADSUtil.buildFromTrace(automaton, trace, state, ADTSymbolNode::new);
    }

    /**
     * Build a single trace ADS from the given information.
     *
     * @param input
     *         the input sequence of the trace
     * @param output
     *         the output sequence of the trace
     * @param finalState
     *         the hypothesis state that should be referenced in the leaf of the ADS
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return the root node of the constructed ADS
     */
    public static <S, I, O> ADTNode<S, I, O> buildADSFromObservation(Word<I> input, Word<O> output, S finalState) {

        if (input.size() != output.size()) {
            throw new IllegalArgumentException("Arguments differ in length");
        }

        final Iterator<I> inputIterator = input.iterator();
        final Iterator<O> outputIterator = output.iterator();

        final ADTNode<S, I, O> result = new ADTSymbolNode<>(null, inputIterator.next());
        ADTNode<S, I, O> nodeIter = result;

        while (inputIterator.hasNext()) {
            final ADTNode<S, I, O> nextNode = new ADTSymbolNode<>(nodeIter, inputIterator.next());
            nodeIter.getChildren().put(outputIterator.next(), nextNode);

            nodeIter = nextNode;
        }

        final ADTNode<S, I, O> finalNode = new ADTLeafNode<>(nodeIter, finalState);
        nodeIter.getChildren().put(outputIterator.next(), finalNode);

        return result;
    }

    /**
     * Tries to merge the given (single trace) ADSs (which only contains one leaf) into the given parent ADSs. If
     * possible, the parent ADS is altered as a side effect
     *
     * @param parent
     *         the parent ADS in which the given child ADS should be merged into
     * @param child
     *         the (single trace) ADS which should be incorporated into the parent ADS.
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return {@code true} if ADSs could be merged, {@code false} otherwise
     */
    public static <S, I, O> boolean mergeADS(ADTNode<S, I, O> parent, ADTNode<S, I, O> child) {

        ADTNode<S, I, O> parentIter = parent, childIter = child;

        while (!(ADTUtil.isLeafNode(parentIter) || ADTUtil.isResetNode(parentIter)) && !ADTUtil.isLeafNode(childIter)) {

            if (!Objects.equals(parentIter.getSymbol(), childIter.getSymbol())) {
                return false;
            }

            final Map<O, ADTNode<S, I, O>> childSuccessors = childIter.getChildren();

            if (childSuccessors.size() != 1) {
                throw new IllegalArgumentException("No single trace child");
            }

            final Map<O, ADTNode<S, I, O>> parentSuccessors = parentIter.getChildren();

            final Map.Entry<O, ADTNode<S, I, O>> childSuccessor = childSuccessors.entrySet().iterator().next();
            final O childOutput = childSuccessor.getKey();
            final ADTNode<S, I, O> childADS = childSuccessor.getValue();

            if (!parentSuccessors.containsKey(childOutput)) {
                parentSuccessors.put(childOutput, childADS);
                childADS.setParent(parentIter);
                return true;
            }

            parentIter = parentSuccessors.get(childOutput);
            childIter = childADS;
        }

        return false;
    }
}
