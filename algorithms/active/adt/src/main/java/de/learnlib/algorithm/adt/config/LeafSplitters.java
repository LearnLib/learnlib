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
package de.learnlib.algorithm.adt.config;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import de.learnlib.algorithm.adt.adt.ADTLeafNode;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.adt.ADTResetNode;
import de.learnlib.algorithm.adt.adt.ADTSymbolNode;
import de.learnlib.algorithm.adt.api.LeafSplitter;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

/**
 * A collection of default {@link LeafSplitter} configurations.
 */
public final class LeafSplitters {

    public static final LeafSplitter DEFAULT_SPLITTER = LeafSplitters::splitIntoNewADS;

    public static final LeafSplitter EXTEND_PARENT = new LeafSplitter() {

        @Override
        public <S, I, O> ADTNode<S, I, O> split(ADTNode<S, I, O> nodeToSplit,
                                                Word<I> distinguishingSuffix,
                                                Word<O> oldOutput,
                                                Word<O> newOutput) {

            if (canSplitParent(nodeToSplit, distinguishingSuffix, oldOutput, newOutput)) {
                return splitParent(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);
            }

            return splitIntoNewADS(nodeToSplit, distinguishingSuffix, oldOutput, newOutput);
        }
    };

    private LeafSplitters() {
        // prevent instantiation
    }

    private static <S, I, O> boolean canSplitParent(ADTNode<S, I, O> nodeToSplit,
                                                    Word<I> distinguishingSuffix,
                                                    Word<O> hypothesisOutput,
                                                    Word<O> newOutput) {

        // initial split
        if (nodeToSplit.getParent() == null) {
            return false;
        }

        final Pair<Word<I>, Word<O>> trace = ADTUtil.buildTraceForNode(nodeToSplit);
        final Word<I> traceInput = trace.getFirst();
        final Word<O> traceOutput = trace.getSecond();

        return traceInput.isPrefixOf(distinguishingSuffix) && traceOutput.isPrefixOf(newOutput) &&
               traceOutput.isPrefixOf(hypothesisOutput);
    }

    private static <S, I, O> ADTNode<S, I, O> splitIntoNewADS(ADTNode<S, I, O> nodeToSplit,
                                                              Word<I> distinguishingSuffix,
                                                              Word<O> oldOutput,
                                                              Word<O> newOutput) {

        final Iterator<I> suffixIter = distinguishingSuffix.iterator();

        // Replace old final state
        final ADTNode<S, I, O> parent = nodeToSplit.getParent();
        final ADTNode<S, I, O> newADS = new ADTSymbolNode<>(null, suffixIter.next());

        if (parent != null) { // if parent == null, we split the initial node
            boolean foundSuccessor = false;
            for (Map.Entry<O, ADTNode<S, I, O>> entry : parent.getChildren().entrySet()) {
                if (entry.getValue().equals(nodeToSplit)) {
                    final ADTNode<S, I, O> reset = new ADTResetNode<>(newADS);

                    reset.setParent(parent);
                    parent.getChildren().put(entry.getKey(), reset);
                    newADS.setParent(reset);

                    foundSuccessor = true;
                    break;
                }
            }

            if (!foundSuccessor) {
                throw new IllegalStateException();
            }
        }

        return finalizeSplit(nodeToSplit, newADS, suffixIter, oldOutput.iterator(), newOutput.iterator());
    }

    private static <S, I, O> ADTNode<S, I, O> finalizeSplit(ADTNode<S, I, O> nodeToSplit,
                                                            ADTNode<S, I, O> adtRoot,
                                                            Iterator<I> suffixIter,
                                                            Iterator<O> oldIter,
                                                            Iterator<O> newIter) {

        ADTNode<S, I, O> previous = adtRoot;
        O oldOut = oldIter.next();
        O newOut = newIter.next();

        while (Objects.equals(oldOut, newOut)) {
            final ADTNode<S, I, O> next = new ADTSymbolNode<>(previous, suffixIter.next());

            previous.getChildren().put(oldOut, next);

            oldOut = oldIter.next();
            newOut = newIter.next();
            previous = next;
        }

        final ADTNode<S, I, O> oldFinalNode = nodeToSplit;
        final ADTNode<S, I, O> newFinalNode = new ADTLeafNode<>(previous, null);

        oldFinalNode.setParent(previous);
        newFinalNode.setParent(previous);

        previous.getChildren().put(oldOut, oldFinalNode);
        previous.getChildren().put(newOut, newFinalNode);

        return newFinalNode;
    }

    public static <S, I, O> ADTNode<S, I, O> splitParent(ADTNode<S, I, O> nodeToSplit,
                                                         Word<I> distinguishingSuffix,
                                                         Word<O> oldOutput,
                                                         Word<O> newOutput) {

        final ADTNode<S, I, O> previousADS = ADTUtil.getStartOfADS(nodeToSplit);

        final Iterator<I> suffixIter = distinguishingSuffix.iterator();
        final Iterator<O> oldIter = oldOutput.iterator();
        final Iterator<O> newIter = newOutput.iterator();
        ADTNode<S, I, O> adsIter = previousADS;
        O newSuffixOutput = null;

        while (!ADTUtil.isLeafNode(adsIter)) {

            // Forward other iterators
            suffixIter.next();
            newIter.next();
            newSuffixOutput = oldIter.next();

            adsIter = adsIter.getChild(newSuffixOutput);
        }

        final ADTNode<S, I, O> parent = adsIter.getParent();
        final ADTNode<S, I, O> continuedADS = new ADTSymbolNode<>(parent, suffixIter.next());

        assert parent != null;
        parent.getChildren().put(newSuffixOutput, continuedADS);

        return finalizeSplit(nodeToSplit, continuedADS, suffixIter, oldIter, newIter);
    }
}
