/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithm.adt.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import de.learnlib.algorithm.adt.adt.ADTLeafNode;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.adt.ADTSymbolNode;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.Pair;
import net.automatalib.graph.ads.ADSNode;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Utility class, that offers some operations revolving around adaptive distinguishing sequences.
 */
public final class ADTUtil {

    private ADTUtil() {
        // prevent instantiation
    }

    public static <S, I, O> boolean isSymbolNode(ADTNode<S, I, O> node) {
        return checkNodeType(node, ADTNode.NodeType.SYMBOL_NODE);
    }

    private static <S, I, O> boolean checkNodeType(ADTNode<S, I, O> node, ADTNode.NodeType type) {
        return node != null && node.getNodeType() == type;
    }

    public static <S, I, O> ADTNode<S, I, O> getStartOfADS(ADTNode<S, I, O> node) {

        ADTNode<S, I, O> iter = node;

        while (iter.getParent() != null && !ADTUtil.isResetNode(iter.getParent())) {
            iter = iter.getParent();
        }

        return iter;
    }

    public static <S, I, O> boolean isResetNode(ADTNode<S, I, O> node) {
        return checkNodeType(node, ADTNode.NodeType.RESET_NODE);
    }

    public static <S, I, O> Set<S> collectHypothesisStates(ADTNode<S, I, O> root) {
        final Set<S> result = new LinkedHashSet<>();
        collectTransformedLeavesRecursively(result, root, ADTNode::getHypothesisState);
        return result;
    }

    public static <S, I, O> Set<ADTNode<S, I, O>> collectLeaves(ADTNode<S, I, O> root) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        collectTransformedLeavesRecursively(result, root, Function.identity());
        return result;
    }

    private static <S, I, O, T> void collectTransformedLeavesRecursively(Set<T> nodes,
                                                                         ADTNode<S, I, O> current,
                                                                         Function<ADTNode<S, I, O>, T> transformer) {
        if (ADTUtil.isLeafNode(current)) {
            nodes.add(transformer.apply(current));
        } else {
            for (ADTNode<S, I, O> n : current.getChildren().values()) {
                collectTransformedLeavesRecursively(nodes, n, transformer);
            }
        }
    }

    public static <S, I, O> Set<ADTNode<S, I, O>> collectADSNodes(ADTNode<S, I, O> root) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        result.add(root);
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

    public static <S, I, O> Set<ADTNode<S, I, O>> collectResetNodes(ADTNode<S, I, O> root) {
        final Set<ADTNode<S, I, O>> result = new LinkedHashSet<>();
        collectResetNodesRecursively(result, root);
        return result;
    }

    private static <S, I, O> void collectResetNodesRecursively(Set<ADTNode<S, I, O>> nodes, ADTNode<S, I, O> current) {
        if (ADTUtil.isResetNode(current)) {
            nodes.add(current);
        }

        for (ADTNode<S, I, O> n : current.getChildren().values()) {
            collectResetNodesRecursively(nodes, n);
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

        ADTNode<S, I, O> parentIter = node.getParent();
        ADTNode<S, I, O> nodeIter = node;
        final WordBuilder<I> inputBuilder = new WordBuilder<>();
        final WordBuilder<O> outputBuilder = new WordBuilder<>();

        while (parentIter != null && !ADTUtil.isResetNode(parentIter)) {
            inputBuilder.append(parentIter.getSymbol());
            outputBuilder.append(ADTUtil.getOutputForSuccessor(parentIter, nodeIter));

            nodeIter = parentIter;
            parentIter = parentIter.getParent();
        }

        return Pair.of(inputBuilder.reverse().toWord(), outputBuilder.reverse().toWord());
    }

    public static <S, I, O> O getOutputForSuccessor(ADTNode<S, I, O> node, ADTNode<S, I, O> successor) {

        if (!node.equals(successor.getParent())) {
            throw new IllegalArgumentException("No parent relationship");
        }

        for (Map.Entry<O, ADTNode<S, I, O>> entry : node.getChildren().entrySet()) {
            if (entry.getValue().equals(successor)) {
                return entry.getKey();
            }
        }

        throw new IllegalArgumentException("No child relationship");
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
            return new ADTLeafNode<>(null, node.getHypothesisState());
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
     * @param <S>
     *         (hypothesis) state type
     * @param <I>
     *         input alphabet type
     * @param <O>
     *         output alphabet type
     *
     * @return the number of encountered reset nodes
     */
    public static <S, I, O> int computeEffectiveResets(ADTNode<S, I, O> adt) {
        return computeEffectiveResetsInternal(adt, 0);
    }

    private static <S, I, O> int computeEffectiveResetsInternal(ADTNode<S, I, O> ads, int accumulatedResets) {
        if (ADTUtil.isLeafNode(ads)) {
            return accumulatedResets;
        }

        final int nextCosts = ADTUtil.isResetNode(ads) ? accumulatedResets + 1 : accumulatedResets;

        return ads.getChildren().values().stream().mapToInt(x -> computeEffectiveResetsInternal(x, nextCosts)).sum();
    }

    public static <S, I, O> Pair<ADTNode<S, I, O>, ADTNode<S, I, O>> buildADSFromTrace(MealyMachine<S, I, ?, O> automaton,
                                                                                       Word<I> trace,
                                                                                       S state) {

        final Iterator<I> sequenceIter = trace.iterator();
        final I input = sequenceIter.next();
        final ADTNode<S, I, O> head = new ADTSymbolNode<>(null, input);

        ADTNode<S, I, O> tempADS = head;
        I tempInput = input;
        S tempState = state;

        while (sequenceIter.hasNext()) {
            final I nextInput = sequenceIter.next();
            final ADTNode<S, I, O> nextNode = new ADTSymbolNode<>(tempADS, nextInput);

            final O oldOutput = automaton.getOutput(tempState, tempInput);

            tempADS.getChildren().put(oldOutput, nextNode);

            tempADS = nextNode;
            tempState = automaton.getSuccessor(tempState, tempInput);
            tempInput = nextInput;
        }

        return Pair.of(head, tempADS);
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
    public static <S, I, O> ADTNode<S, I, O> buildADSFromObservation(Word<I> input,
                                                                     Word<O> output,
                                                                     S finalState) {

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

    public static <S, I, O> boolean isLeafNode(ADTNode<S, I, O> node) {
        return checkNodeType(node, ADTNode.NodeType.LEAF_NODE);
    }
}
