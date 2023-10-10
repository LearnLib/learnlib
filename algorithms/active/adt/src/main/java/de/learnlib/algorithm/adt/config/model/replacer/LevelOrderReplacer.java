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
package de.learnlib.algorithm.adt.config.model.replacer;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADT;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.api.SubtreeReplacer;
import de.learnlib.algorithm.adt.config.model.ADSCalculator;
import de.learnlib.algorithm.adt.model.ReplacementResult;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;

public class LevelOrderReplacer implements SubtreeReplacer {

    private final ADSCalculator adsCalculator;

    public LevelOrderReplacer(ADSCalculator adsProvider) {
        this.adsCalculator = adsProvider;
    }

    @Override
    public <S, I, O> Set<ReplacementResult<S, I, O>> computeReplacements(MealyMachine<S, I, ?, O> hypothesis,
                                                                         Alphabet<I> inputs,
                                                                         ADT<S, I, O> adt) {

        // if we cannot save any resets, don't bother with replacement
        if (ADTUtil.collectResetNodes(adt.getRoot()).isEmpty()) {
            return Collections.emptySet();
        }

        final Set<ReplacementResult<S, I, O>> result = new LinkedHashSet<>();
        final Queue<ADTNode<S, I, O>> queue = new LinkedList<>();

        queue.add(adt.getRoot());

        while (!queue.isEmpty()) {
            final ADTNode<S, I, O> node = queue.poll();
            final Set<S> targetStates = ADTUtil.collectHypothesisStates(node);

            // try to extendLeaf the parent ADS

            // cannot extendLeaf parent
            if (!adt.getRoot().equals(node)) {
                final ReplacementResult<S, I, O> replacementResult =
                        SingleReplacer.computeParentExtension(hypothesis, inputs, node, targetStates, adsCalculator);

                if (replacementResult != null) {
                    result.add(replacementResult);
                    continue;
                }
            }

            // if we cannot save any resets, don't bother with replacement
            if (ADTUtil.collectResetNodes(node).isEmpty()) {
                continue;
            }

            // compute ADS for complete subtree
            final Optional<ADTNode<S, I, O>> potentialADS = adsCalculator.compute(hypothesis, inputs, targetStates);

            if (potentialADS.isPresent()) {
                result.add(new ReplacementResult<>(node, potentialADS.get()));
                continue;
            }
            queue.addAll(ADTUtil.collectDirectSubADSs(node));
        }

        return result;
    }
}
