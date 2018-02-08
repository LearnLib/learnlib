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
package de.learnlib.algorithms.adt.adt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.api.LeafSplitter;
import de.learnlib.algorithms.adt.config.LeafSplitters;
import de.learnlib.algorithms.adt.util.ADTUtil;
import de.learnlib.api.oracle.SymbolQueryOracle;
import net.automatalib.words.Word;

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
 *
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ADT<S, I, O> implements Serializable {

    private ADTNode<S, I, O> root;

    private transient LeafSplitter leafSplitter;

    public ADT(final LeafSplitter leafSplitter) {
        this.leafSplitter = leafSplitter;
    }

    /**
     * Initializes the ADT with a single leaf node.
     *
     * @param state
     *         the referenced state of the leaf
     */
    public void initialize(final S state) {
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
    public void replaceNode(final ADTNode<S, I, O> oldNode, final ADTNode<S, I, O> newNode) {

        if (this.root.equals(oldNode)) {
            this.root = newNode;
        } else if (ADTUtil.isResetNode(oldNode)) {
            final ADTNode<S, I, O> endOfPreviousADS = oldNode.getParent();
            final O outputToReset = ADTUtil.getOutputForSuccessor(endOfPreviousADS, oldNode);

            newNode.setParent(endOfPreviousADS);
            endOfPreviousADS.getChildren().put(outputToReset, newNode);
        } else {
            final ADTNode<S, I, O> oldNodeParent = oldNode.getParent(); //reset node

            assert ADTUtil.isResetNode(oldNodeParent);

            final ADTNode<S, I, O> endOfPreviousADS = oldNodeParent.getParent();
            final O outputToReset = ADTUtil.getOutputForSuccessor(endOfPreviousADS, oldNodeParent);
            final ADTNode<S, I, O> newResetNode = new ADTResetNode<>(newNode);

            newResetNode.setParent(endOfPreviousADS);
            newNode.setParent(newResetNode);
            endOfPreviousADS.getChildren().put(outputToReset, newResetNode);
        }
    }

    /**
     * Successively sifts a word through the ADT induced by the given node. Stops when reaching a leaf.
     *
     * @param word
     *         the word to sift
     * @param subtree
     *         the node whose subtree is considered
     *
     * @return the leaf (see {@link ADTNode#sift(SymbolQueryOracle, Word)})
     */
    public ADTNode<S, I, O> sift(final SymbolQueryOracle<I, O> oracle,
                                 final Word<I> word,
                                 final ADTNode<S, I, O> subtree) {

        ADTNode<S, I, O> current = subtree;

        while (!ADTUtil.isLeafNode(current)) {
            current = current.sift(oracle, word);
        }

        return current;
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
     *
     * @return the new leaf node
     */
    public ADTNode<S, I, O> extendLeaf(final ADTNode<S, I, O> nodeToSplit,
                                       final Word<I> distinguishingSuffix,
                                       final Word<O> oldOutput,
                                       final Word<O> newOutput) {

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
            return splitLeaf(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);
        }
        return LeafSplitters.splitParent(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);
    }

    /**
     * Splits a leaf node using the local {@link LeafSplitter}.
     *
     * @param nodeToSplit
     *         the leaf node to split
     * @param distinguishingSuffix
     *         the input sequence that splits the hypothesis state of the leaf to split and the new node
     * @param oldOutput
     *         the hypothesis output of the node to split given the distinguishing suffix
     * @param newOutput
     *         the hypothesis output of the new leaf given the distinguishing suffix
     *
     * @return the new leaf node
     */
    public ADTNode<S, I, O> splitLeaf(final ADTNode<S, I, O> nodeToSplit,
                                      final Word<I> distinguishingSuffix,
                                      final Word<O> oldOutput,
                                      final Word<O> newOutput) {

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

        final ADTNode<S, I, O> result =
                this.leafSplitter.split(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);

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
    public LCAInfo<S, I, O> findLCA(final ADTNode<S, I, O> s1, final ADTNode<S, I, O> s2) {

        final Map<ADTNode<S, I, O>, ADTNode<S, I, O>> s1ParentsToS1 = new HashMap<>();

        ADTNode<S, I, O> s1Iter = s1;
        ADTNode<S, I, O> s2Iter = s2;

        while (s1Iter.getParent() != null) {
            s1ParentsToS1.put(s1Iter.getParent(), s1Iter);
            s1Iter = s1Iter.getParent();
        }

        final Set<ADTNode<S, I, O>> s1Parents = s1ParentsToS1.keySet();

        while (s2Iter.getParent() != null) {

            if (s1Parents.contains(s2Iter.getParent())) {
                if (!ADTUtil.isSymbolNode(s2Iter.getParent())) {
                    throw new IllegalStateException("Only Symbol Nodes should be LCAs");
                }

                final ADTNode<S, I, O> lca = s2Iter.getParent();
                final O s1Out = ADTUtil.getOutputForSuccessor(lca, s1ParentsToS1.get(lca));
                final O s2Out = ADTUtil.getOutputForSuccessor(lca, s2Iter);

                return new LCAInfo<>(lca, s1Out, s2Out);
            }

            s2Iter = s2Iter.getParent();
        }

        throw new IllegalStateException("Nodes do not share a parent node");
    }

    public void setLeafSplitter(final LeafSplitter leafSplitter) {
        this.leafSplitter = leafSplitter;
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
