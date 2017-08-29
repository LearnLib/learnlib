/* Copyright (C) 2017 TU Dortmund
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

import de.learnlib.algorithms.adt.adt.ADT;
import de.learnlib.algorithms.adt.adt.ADTNode;
import de.learnlib.algorithms.adt.api.SubtreeReplacer;
import de.learnlib.algorithms.adt.config.model.ADSCalculator;
import de.learnlib.algorithms.adt.model.ReplacementResult;
import de.learnlib.algorithms.adt.util.ADTUtil;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author frohme
 */
@ParametersAreNonnullByDefault
public class LevelOrderReplacer implements SubtreeReplacer {

	private final ADSCalculator adsCalculator;

	public LevelOrderReplacer(final ADSCalculator adsProvider) {
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

		final Set<ReplacementResult<S, I, O>> result = new LinkedHashSet<>();
		final Queue<ADTNode<S, I, O>> queue = new LinkedList<>();

		queue.add(adt.getRoot());

		replacementLoop:
		while (!queue.isEmpty()) {
			final ADTNode<S, I, O> node = queue.poll();
			final Set<S> targetStates =
					ADTUtil.collectLeaves(node).stream().map(ADTNode::getHypothesisState).collect(Collectors.toSet());

			// try to extendLeaf the parent ADS
			extensionCase:

			// cannot extendLeaf parent
			if (!adt.getRoot().equals(node)) {
				final ADTNode<S, I, O> parentReset = node.getParent();

				assert ADTUtil.isResetNode(parentReset) : new IllegalStateException("should not happen");

				final Word<I> incomingTraceInput = ADTUtil.buildTraceForNode(parentReset).getFirst();

				Map<S, S> currentToInitialMapping =
						targetStates.stream().collect(Collectors.toMap(Function.identity(), Function.identity()));
				for (final I i : incomingTraceInput) {

					final Map<S, S> nextMapping = new HashMap<>();

					for (final S s : currentToInitialMapping.keySet()) {
						final S successor = hypothesis.getSuccessor(s, i);

						// converging states
						if (nextMapping.containsKey(successor)) {
							break extensionCase;
						}

						nextMapping.put(successor, currentToInitialMapping.get(s));
					}

					currentToInitialMapping = nextMapping;
				}

				final Optional<ADTNode<S, I, O>> potentialExtension =
						adsCalculator.compute(hypothesis, inputs, currentToInitialMapping.keySet());

				if (potentialExtension.isPresent()) {

					final ADTNode<S, I, O> extension = potentialExtension.get();

					for (final ADTNode<S, I, O> finalNode : ADTUtil.collectLeaves(extension)) {
						finalNode.setHypothesisState(currentToInitialMapping.get(finalNode.getHypothesisState()));
					}

					result.add(new ReplacementResult<>(parentReset, potentialExtension.get()));
					continue replacementLoop;
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