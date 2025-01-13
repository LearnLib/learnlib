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
package de.learnlib.algorithm.adt.adt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.learnlib.algorithm.adt.api.LeafSplitter;
import de.learnlib.algorithm.adt.config.LeafSplitters;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.word.Word;

/**
 * The adaptive discrimination tree class. Essentially holds a reference to the root {@link ADTNode} and offers some
 * utility methods used throughout the learning process.
 *
 * @param <S>
 *         (hypothesis) state type
 * @param <I>
 *         input alphabet type
 * @param <O>
 *         output alphabet type
 */
public class ADT<S, I, O> {

    private ADTNode<S, I, O> root;

    /**
     * Initializes the ADT with a single leaf node.
     *
     * @param state
     *         the referenced state of the leaf
     */
    public void initialize(S state) {
        this.root = new ADTLeafNode<>(null, state);
    }

    /**
     * Returns the root node of this ADT.
     *
     * @return the root
     */
    public ADTNode<S, I, O> getRoot() {
        return this.root;
    }

    /**
     * Replaces an existing node in the tree with a new one and updates the references of parent/child nodes
     * accordingly.
     *
     * @param oldNode
     *         the node to replace
     * @param newNode
     *         the replacement
     */
    public void replaceNode(ADTNode<S, I, O> oldNode, ADTNode<S, I, O> newNode) {

        if (this.root.equals(oldNode)) {
            this.root = newNode;
        } else if (ADTUtil.isResetNode(oldNode)) {
            final ADTNode<S, I, O> endOfPreviousADS = oldNode.getParent();
            assert endOfPreviousADS != null;
            final O outputToReset = ADTUtil.getOutputForSuccessor(endOfPreviousADS, oldNode);

            newNode.setParent(endOfPreviousADS);
            endOfPreviousADS.getChildren().put(outputToReset, newNode);
        } else {
            final ADTNode<S, I, O> oldNodeParent = oldNode.getParent(); //reset node

            assert ADTUtil.isResetNode(oldNodeParent);

            final ADTNode<S, I, O> endOfPreviousADS = oldNodeParent.getParent();
            assert endOfPreviousADS != null;

            final O outputToReset = ADTUtil.getOutputForSuccessor(endOfPreviousADS, oldNodeParent);
            final ADTNode<S, I, O> newResetNode = new ADTResetNode<>(newNode);

            newResetNode.setParent(endOfPreviousADS);
            newNode.setParent(newResetNode);
            endOfPreviousADS.getChildren().put(outputToReset, newResetNode);
        }
    }

    /**
     * Splitting a leaf node by extending the trace leading into the node to split.
     *
     * @param nodeToSplit
     *         the leaf node to extends
     * @param distinguishingSuffix
     *         the input sequence that splits the hypothesis state of the leaf to split and the new node. The current
     *         trace leading into the node to split must be a prefix of this word.
     * @param oldOutput
     *         the hypothesis output of the node to split given the distinguishing suffix
     * @param newOutput
     *         the hypothesis output of the new leaf given the distinguishing suffix
     * @param leafSplitter
     *         the split strategy in case the root node needs to be split
     *
     * @return the new leaf node
     */
    public ADTNode<S, I, O> extendLeaf(ADTNode<S, I, O> nodeToSplit,
                                       Word<I> distinguishingSuffix,
                                       Word<O> oldOutput,
                                       Word<O> newOutput,
                                       LeafSplitter leafSplitter) {

        if (!ADTUtil.isLeafNode(nodeToSplit)) {
            throw new IllegalArgumentException("Node to split is not a leaf node");
        }
        if (!(distinguishingSuffix.length() == oldOutput.length() && oldOutput.length() == newOutput.length())) {
            throw new IllegalArgumentException("Distinguishing suffixes and outputs differ in length");
        }
        if (oldOutput.equals(newOutput)) {
            throw new IllegalArgumentException("Old and new output are equal");
        }

        // initial split
        if (this.root.equals(nodeToSplit)) {
            return splitLeaf(nodeToSplit, distinguishingSuffix, oldOutput, newOutput, leafSplitter);
        }
        return LeafSplitters.splitParent(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);
    }

    /**
     * Splits a leaf node using a given {@link LeafSplitter}.
     *
     * @param nodeToSplit
     *         the leaf node to split
     * @param distinguishingSuffix
     *         the input sequence that splits the hypothesis state of the leaf to split and the new node
     * @param oldOutput
     *         the hypothesis output of the node to split given the distinguishing suffix
     * @param newOutput
     *         the hypothesis output of the new leaf given the distinguishing suffix
     * @param leafSplitter
     *         the split strategy for leaves
     *
     * @return the new leaf node
     */
    public ADTNode<S, I, O> splitLeaf(ADTNode<S, I, O> nodeToSplit,
                                      Word<I> distinguishingSuffix,
                                      Word<O> oldOutput,
                                      Word<O> newOutput,
                                      LeafSplitter leafSplitter) {

        if (!ADTUtil.isLeafNode(nodeToSplit)) {
            throw new IllegalArgumentException("Node to split is not a final node");
        }
        if (!(distinguishingSuffix.length() == oldOutput.length() && oldOutput.length() == newOutput.length())) {
            throw new IllegalArgumentException("Distinguishing suffixes and outputs differ in length");
        }
        if (oldOutput.equals(newOutput)) {
            throw new IllegalArgumentException("Old and new output are equal");
        }

        final boolean wasRoot = this.root.equals(nodeToSplit);

        final ADTNode<S, I, O> result = leafSplitter.split(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);

        if (wasRoot) {
            this.root = ADTUtil.getStartOfADS(nodeToSplit);
        }

        return result;
    }

    /**
     * Return the lowest common ancestor for the given two nodes.
     *
     * @param s1
     *         first node
     * @param s2
     *         second node
     *
     * @return A {@link LCAInfo} containing the lowest common {@link ADTNode}, the output determining the subtree of the
     * first node and the output determining the subtree of the second node
     */
    public LCAInfo<S, I, O> findLCA(ADTNode<S, I, O> s1, ADTNode<S, I, O> s2) {

        final Map<ADTNode<S, I, O>, ADTNode<S, I, O>> s1ParentsToS1 = new HashMap<>();

        ADTNode<S, I, O> s1Iter = s1;
        ADTNode<S, I, O> s1ParentIter = s1.getParent();

        while (s1ParentIter != null) {
            s1ParentsToS1.put(s1ParentIter, s1Iter);
            s1Iter = s1ParentIter;
            s1ParentIter = s1ParentIter.getParent();
        }

        final Set<ADTNode<S, I, O>> s1Parents = s1ParentsToS1.keySet();

        ADTNode<S, I, O> s2Iter = s2;
        ADTNode<S, I, O> s2ParentIter = s2.getParent();

        while (s2ParentIter != null) {

            if (s1Parents.contains(s2ParentIter)) {
                if (!ADTUtil.isSymbolNode(s2ParentIter)) {
                    throw new IllegalStateException("Only Symbol Nodes should be LCAs");
                }

                final ADTNode<S, I, O> lca = s2ParentIter;
                final O s1Out = ADTUtil.getOutputForSuccessor(lca, s1ParentsToS1.get(lca));
                final O s2Out = ADTUtil.getOutputForSuccessor(lca, s2Iter);

                return new LCAInfo<>(lca, s1Out, s2Out);
            }

            s2Iter = s2ParentIter;
            s2ParentIter = s2ParentIter.getParent();
        }

        throw new IllegalStateException("Nodes do not share a parent node");
    }

    public static class LCAInfo<S, I, O> {

        public final ADTNode<S, I, O> adtNode;
        public final O firstOutput;
        public final O secondOutput;

        LCAInfo(ADTNode<S, I, O> adtNode, O firstOutput, O secondOutput) {
            this.adtNode = adtNode;
            this.firstOutput = firstOutput;
            this.secondOutput = secondOutput;
        }
    }
}
