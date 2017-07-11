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
package de.learnlib.algorithms.rpni.util;

import de.learnlib.algorithms.rpni.automata.CPTA;
import de.learnlib.algorithms.rpni.automata.PTA;
import net.automatalib.words.Word;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * @author frohme
 */
public class RPNIUtil {

	public static <I> PTA<I> merge(final PTA<I> pta, final Integer rootState, final Integer stateToMerge) {

		final PTA<I> copy = new PTA<>(pta);

		return mergeInternal(pta, copy, rootState, stateToMerge, (a, b) -> {});
	}

	public static <I> CPTA<I> merge(final CPTA<I> pta, final Integer rootState, final Integer stateToMerge) {

		final CPTA<I> copy = new CPTA<>(pta);

		return mergeInternal(pta,
							 copy,
							 rootState,
							 stateToMerge,
							 (a, b) -> copy.setCoverage(a, copy.getCoverage(a) + copy.getCoverage(b)));
	}

	private static <I, M extends PTA<I>> M mergeInternal(final M src,
														 final M copy,
														 final Integer rootState,
														 final Integer stateToMerge,
														 final BiConsumer<Integer, Integer> preProcessing) {

		final Integer parentNode = src.getParentNode(stateToMerge);
		final I parentNodeInput = src.getParentNodeInput(stateToMerge);

		copy.removeTransition(parentNode, parentNodeInput, stateToMerge);
		copy.addTransition(parentNode, parentNodeInput, rootState);

		fold(copy, rootState, stateToMerge, preProcessing);

		return copy;
	}

	private static <I> void fold(final PTA<I> pta,
								 final Integer rootState,
								 final Integer stateToMerge,
								 final BiConsumer<Integer, Integer> preProcessing) {

		preProcessing.accept(rootState, stateToMerge);

		if (pta.isAccepting(stateToMerge)) {
			pta.setAccepting(rootState, true);
		}

		for (final I i : pta.getInputAlphabet()) {
			final Integer stateToMergeSuccessor = pta.getSuccessor(stateToMerge, i);

			if (stateToMergeSuccessor != null) {
				final Integer rootStateSuccessor = pta.getSuccessor(rootState, i);

				if (rootStateSuccessor != null) {
					fold(pta, rootStateSuccessor, stateToMergeSuccessor, preProcessing);
				}
				else {
					pta.removeTransition(stateToMerge, i, stateToMergeSuccessor);
					pta.setTransition(rootState, i, stateToMergeSuccessor);

					pta.setParentNode(stateToMergeSuccessor, rootState);
					pta.setParentNodeInput(stateToMergeSuccessor, i);
				}
			}
		}

		pta.removeState(stateToMerge);
	}

	public static <I> boolean isCompatible(final PTA<I> pta, final Collection<Word<I>> negativeSamples) {

		for (final Word<I> w : negativeSamples) {
			final Integer reachedState = pta.getState(w);

			if (reachedState != null && pta.isAccepting(reachedState)) {
				return false;
			}
		}

		return true;
	}

	public static <I> void promote(final PTA<I> pta,
								   final Collection<Integer> redStates,
								   final Collection<Integer> blueStates,
								   final Integer stateToPromote) {

		redStates.add(stateToPromote);

		for (final I i : pta.getInputAlphabet()) {
			final Integer stateToPromoteSuccessor = pta.getSuccessor(stateToPromote, i);

			if (stateToPromoteSuccessor != null) {
				blueStates.add(stateToPromoteSuccessor);
			}
		}
	}

}
