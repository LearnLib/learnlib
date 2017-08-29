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
package de.learnlib.algorithms.rpni;

import de.learnlib.algorithms.rpni.automata.PTA;
import de.learnlib.algorithms.rpni.util.EDSMUtil;
import de.learnlib.algorithms.rpni.util.PTAUtil;
import de.learnlib.algorithms.rpni.util.RPNIUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * "Evidence-Driven State Merging" algorithm. Implementation based on the book "Grammatical Inference" by de la Higuera.
 *
 * @param <I> input alphabet type
 *
 * @author frohme
 */
public class EDSMLearner<I> extends AbstractStateMergingLearner<I, PTA<I>, EDSMLearner.EDSMComputationResult<I>> {

	static class EDSMComputationResult<I> {

		final PTA<I> pta;
		final float score;

		public EDSMComputationResult(final PTA<I> pta, final float score) {
			this.pta = pta;
			this.score = score;
		}

		public PTA<I> getPta() {
			return this.pta;
		}

		public float getScore() {
			return this.score;
		}
	}

	// TODO maybe extend super class to cover this semantic as well
	@Override
	public PTA<I> computeModel() {
		PTA<I> result = getInitialModel();

		final Set<Integer> redStates = new HashSet<>();
		final Set<Integer> blueStates = new HashSet<>();

		redStates.add(result.getInitialState());

		for (final I i : result.getInputAlphabet()) {
			Integer rootSucc = result.getSuccessor(result.getInitialState(), i);
			if (rootSucc != null) {
				blueStates.add(rootSucc);
			}
		}

		final List<Future<Optional<EDSMComputationResult<I>>>> searchTasks = new ArrayList<>(result.size());
		final Comparator<Integer> blueStateComparator = getBlueStateComparator(result);
		final Comparator<Integer> redStateComparator = getRedStateComparator(result);

		while (!blueStates.isEmpty()) {
			final PTA<I> finalCopyOfCurrentResult = result;

			final List<Integer> sortedRedStates = new ArrayList<>(redStates);
			final List<Integer> sortedBlueStates = new ArrayList<>(blueStates);

			Collections.sort(sortedRedStates, redStateComparator);
			Collections.sort(sortedBlueStates, blueStateComparator);

			boolean foundNonMergeableBlueNode = false;
			PTA<I> bestResult = null;
			float bestScore = Float.NEGATIVE_INFINITY;
			Integer bestBlue = null;

			blueLoop:
			for (final Integer blueState : sortedBlueStates) {
				for (final Integer redState : sortedRedStates) {
					searchTasks.add(super.threadPoolExecutor.submit(() -> computeMergedModel(finalCopyOfCurrentResult,
																							 redState,
																							 blueState)));
				}

				boolean foundMergeableStates = false;

				try {
					for (final Future<Optional<EDSMComputationResult<I>>> f : searchTasks) {
						final Optional<EDSMComputationResult<I>> temporaryResult = f.get();

						if (temporaryResult.isPresent()) {
							foundMergeableStates = true;

							final EDSMComputationResult<I> edsmResult = temporaryResult.get();
							if (edsmResult.getScore() > bestScore) {
								bestBlue = blueState;
								bestResult = edsmResult.getPta();
								bestScore = edsmResult.getScore();
							}
						}
					}
				}
				catch (InterruptedException | ExecutionException ee) {
					// This should not happen
					throw new RuntimeException(ee);
				}
				finally {
					for (final Future<Optional<EDSMComputationResult<I>>> f : searchTasks) {
						f.cancel(true);
					}
					searchTasks.clear();
				}

				if (!foundMergeableStates) {
					blueStates.remove(blueState);
					RPNIUtil.promote(result, redStates, blueStates, blueState);
					foundNonMergeableBlueNode = true;
					break blueLoop;
				}
			}

			if (!foundNonMergeableBlueNode) {
				blueStates.remove(bestBlue);
				result = bestResult;

				for (final Integer red : redStates) {
					for (final I i : result.getInputAlphabet()) {
						final Integer redSuccessor = result.getSuccessor(red, i);

						if (redSuccessor != null && !redStates.contains(redSuccessor)) {
							blueStates.add(redSuccessor);
						}
					}
				}
			}
		}

		updateStateAcceptance(result, positiveSamples, true);
		updateStateAcceptance(result, negativeSamples, false);

		return result;
	}

	@Override
	protected PTA<I> getInitialModel() {
		final PTA<I> result = new PTA<>(PTAUtil.buildAlphabetFromSamples(positiveSamples, negativeSamples));
		PTAUtil.fillWithSamples(result, positiveSamples, Collections.emptySet());

		return result;
	}

	@Override
	protected Optional<EDSMComputationResult<I>> computeMergedModel(PTA<I> model, Integer redState, Integer blueState) {
		final PTA<I> mergedAutomaton = RPNIUtil.merge(model, redState, blueState);
		final float score = EDSMUtil.count(mergedAutomaton, positiveSamples, negativeSamples);

		if (score > Float.NEGATIVE_INFINITY) {
			return Optional.of(new EDSMComputationResult<>(mergedAutomaton, score));
		}

		return Optional.empty();
	}

	@Override
	protected PTA<I> extractMergedModel(EDSMComputationResult<I> data) {
		return data.getPta();
	}
}
