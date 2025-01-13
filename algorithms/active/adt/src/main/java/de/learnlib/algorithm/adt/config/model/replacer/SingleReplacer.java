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
package de.learnlib.algorithm.adt.config.model.replacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADT;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.api.SubtreeReplacer;
import de.learnlib.algorithm.adt.config.model.ADSCalculator;
import de.learnlib.algorithm.adt.model.ReplacementResult;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.smartcollection.ReflexiveMapView;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SingleReplacer implements SubtreeReplacer {

    private final ADSCalculator adsCalculator;

    public SingleReplacer(ADSCalculator adsProvider) {
        this.adsCalculator = adsProvider;
    }

    @Override
    public <S, I, O> Set<ReplacementResult<S, I, O>> computeReplacements(MealyMachine<S, I, ?, O> hypothesis,
                                                                         Alphabet<I> inputs,
                                                                         ADT<S, I, O> adt) {

        final Set<ADTNode<S, I, O>> candidates = ADTUtil.collectADSNodes(adt.getRoot(), false);

        // cache scores to prevent expensive recalculations during sorting
        final List<Pair<ADTNode<S, I, O>, Double>> sortedCandidates = new ArrayList<>(candidates.size());
        for (ADTNode<S, I, O> candidate : candidates) {
            final int resets = 1 + ADTUtil.collectResetNodes(candidate).size();
            final int leaves = ADTUtil.collectLeaves(candidate).size();
            sortedCandidates.add(Pair.of(candidate, resets / (double) leaves));
        }
        sortedCandidates.sort(Comparator.comparingDouble(Pair::getSecond));

        for (Pair<ADTNode<S, I, O>, Double> candidate : sortedCandidates) {
            final ADTNode<S, I, O> node = candidate.getFirst();
            final Set<S> targetStates = ADTUtil.collectHypothesisStates(node);

            // check if we can extendLeaf the parent ADS
            final ReplacementResult<S, I, O> replacementResult =
                    computeParentExtension(hypothesis, inputs, node, targetStates, adsCalculator);

            if (replacementResult != null) {
                return Collections.singleton(replacementResult);
            }

            // if we cannot save any resets, don't bother with replacement
            if (ADTUtil.collectResetNodes(node).isEmpty()) {
                continue;
            }

            final Optional<ADTNode<S, I, O>> potentialADS = adsCalculator.compute(hypothesis, inputs, targetStates);

            if (potentialADS.isPresent()) {
                return Collections.singleton(new ReplacementResult<>(node, potentialADS.get()));
            }
        }

        return Collections.emptySet();
    }

    /**
     * Try to compute a replacement for an ADT subtree that extends the parent ADS.
     *
     * @param hypothesis
     *         the hypothesis for determining the system behavior
     * @param inputs
     *         the input symbols to consider
     * @param node
     *         the root node of the sub-ADT
     * @param targetStates
     *         the set of hypothesis states covered by the given ADT node
     * @param adsCalculator
     *         the ADS calculator instance
     * @param <S>
     *         state type
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return a ReplacementResult for the parent (reset) node, if a valid replacement is found. {@code null} otherwise.
     */
    static <S, I, O> @Nullable ReplacementResult<S, I, O> computeParentExtension(MealyMachine<S, I, ?, O> hypothesis,
                                                                                 Alphabet<I> inputs,
                                                                                 ADTNode<S, I, O> node,
                                                                                 Set<S> targetStates,
                                                                                 ADSCalculator adsCalculator) {
        final ADTNode<S, I, O> parentReset = node.getParent();
        assert ADTUtil.isResetNode(parentReset) : "should not happen";

        final Word<I> incomingTraceInput = ADTUtil.buildTraceForNode(parentReset).getFirst();

        Map<S, S> currentToInitialMapping = new ReflexiveMapView<>(targetStates);
        for (I i : incomingTraceInput) {

            final Map<S, S> nextMapping = new HashMap<>();

            for (Map.Entry<S, S> entry : currentToInitialMapping.entrySet()) {
                final S successor = hypothesis.getSuccessor(entry.getKey(), i);

                // converging states
                if (nextMapping.containsKey(successor)) {
                    return null;
                }

                nextMapping.put(successor, entry.getValue());
            }

            currentToInitialMapping = nextMapping;
        }

        final Optional<ADTNode<S, I, O>> potentialExtension =
                adsCalculator.compute(hypothesis, inputs, currentToInitialMapping.keySet());

        if (potentialExtension.isPresent()) {

            final ADTNode<S, I, O> extension = potentialExtension.get();

            for (ADTNode<S, I, O> finalNode : ADTUtil.collectLeaves(extension)) {
                finalNode.setState(currentToInitialMapping.get(finalNode.getState()));
            }

            return new ReplacementResult<>(parentReset, potentialExtension.get());
        }

        return null;
    }
}
