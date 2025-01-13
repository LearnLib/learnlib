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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADT;
import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.api.SubtreeReplacer;
import de.learnlib.algorithm.adt.config.model.ADSCalculator;
import de.learnlib.algorithm.adt.model.ReplacementResult;
import de.learnlib.algorithm.adt.util.ADTUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.HashUtil;

public class ExhaustiveReplacer implements SubtreeReplacer {

    private final ADSCalculator adsCalculator;

    public ExhaustiveReplacer(ADSCalculator adsProvider) {
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

        final Set<S> statesAsSet = new HashSet<>(hypothesis.getStates());
        final Optional<ADTNode<S, I, O>> potentialResult = adsCalculator.compute(hypothesis, inputs, statesAsSet);

        if (potentialResult.isPresent()) {
            return Collections.singleton(new ReplacementResult<>(adt.getRoot(), potentialResult.get()));
        }

        final Set<ADTNode<S, I, O>> candidates = ADTUtil.collectADSNodes(adt.getRoot(), false);

        final PriorityQueue<Set<S>> queue = new PriorityQueue<>(candidates.size(), Comparator.comparingInt(Set::size));
        for (ADTNode<S, I, O> node : candidates) {
            final Set<ADTNode<S, I, O>> leaves = ADTUtil.collectLeaves(node);
            final Set<S> set = new LinkedHashSet<>(HashUtil.capacity(leaves.size()));

            for (ADTNode<S, I, O> l : leaves) {
                set.add(l.getState());
            }

            queue.add(set);
        }

        while (!queue.isEmpty()) {
            final Set<S> finalNodes = queue.remove();
            final Set<S> targets = new HashSet<>(statesAsSet);
            targets.removeAll(finalNodes);

            if (targets.size() < 2) {
                continue;
            }

            final Optional<ADTNode<S, I, O>> alt = adsCalculator.compute(hypothesis, inputs, targets);

            if (alt.isPresent()) {
                return Collections.singleton(new ReplacementResult<>(adt.getRoot(), alt.get(), finalNodes));
            }
        }

        return Collections.emptySet();
    }
}
