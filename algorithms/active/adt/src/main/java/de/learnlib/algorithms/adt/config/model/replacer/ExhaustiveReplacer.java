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
package de.learnlib.algorithms.adt.config.model.replacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.algorithms.adt.adt.ADT;
import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.SubtreeReplacer;
import de.learnlib.algorithms.adt.config.model.ADSCalculator;
import de.learnlib.algorithms.adt.model.ReplacementResult;
import de.learnlib.algorithms.adt.util.ADTUtil;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class ExhaustiveReplacer implements SubtreeReplacer {

    private final ADSCalculator adsCalculator;

    public ExhaustiveReplacer(final ADSCalculator adsProvider) {
        this.adsCalculator = adsProvider;
    }

    @Override
    public <S, I, O> Set<ReplacementResult<S, I, O>> computeReplacements(final MealyMachine<S, I, ?, O> hypothesis,
                                                                         final Alphabet<I> inputs,
                                                                         final ADT<S, I, O> adt) {
        // if we cannot save any resets, don't bother with replacement
        if (ADTUtil.collectResetNodes(adt.getRoot()).isEmpty()) {
            return Collections.emptySet();
        }

        final Set<S> statesAsSet = new HashSet<>(hypothesis.getStates());
        final Optional<ADTNode<S, I, O>> potentialResult = adsCalculator.compute(hypothesis, inputs, statesAsSet);

        if (potentialResult.isPresent()) {
            return Collections.singleton(new ReplacementResult<>(adt.getRoot(), potentialResult.get()));
        }

        final Set<ADTNode<S, I, O>> candidates = ADTUtil.collectADSNodes(adt.getRoot());
        candidates.remove(adt.getRoot());

        final Map<ADTNode<S, I, O>, Set<S>> subtreesToFinalNodes = candidates.stream()
                                                                             .collect(Collectors.toMap(Function.identity(),
                                                                                                       node -> ADTUtil.collectLeaves(
                                                                                                               node)
                                                                                                                      .stream()
                                                                                                                      .map(ADTNode::getHypothesisState)
                                                                                                                      .collect(
                                                                                                                              Collectors
                                                                                                                                      .toSet())));

        final List<ADTNode<S, I, O>> sortedCandidates = new ArrayList<>(candidates);
        Collections.sort(sortedCandidates, Comparator.comparingInt(n -> subtreesToFinalNodes.get(n).size()));

        for (final ADTNode<S, I, O> node : sortedCandidates) {

            final Set<S> finalNodes = subtreesToFinalNodes.get(node);
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
