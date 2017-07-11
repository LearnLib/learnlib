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
import de.learnlib.algorithms.rpni.util.RPNIUtil;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.passive.api.PassiveDFALearner;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Abstract super class for passive learning algorithms based on the state-merging approach. This class starts by
 * constructing a prefix-tree acceptor and traverses the PTA in a breadth-first manner, looking for pairs of states to
 * merge. For reference, see the "blue-fringe" approach.
 *
 * @param <I> input alphabet type
 * @param <M> model type of hypothesis
 * @param <D> data type of intermediate merge results
 *
 * @author frohme
 */
public abstract class AbstractStateMergingLearner<I, M extends PTA<I>, D> implements PassiveDFALearner<I> {

	protected final ThreadPoolExecutor threadPoolExecutor;

	protected final Collection<Word<I>> positiveSamples;
	protected final Collection<Word<I>> negativeSamples;

	protected AbstractStateMergingLearner() {
		this.positiveSamples = new LinkedHashSet<>();
		this.negativeSamples = new LinkedHashSet<>();

		final int processors = Runtime.getRuntime().availableProcessors();
		threadPoolExecutor =
				new ThreadPoolExecutor(processors, processors, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		threadPoolExecutor.allowCoreThreadTimeOut(true);
	}

	@Override
	public void addSamples(Collection<? extends DefaultQuery<I, Boolean>> samples) {
		for (final DefaultQuery<I, Boolean> s : samples) {
			if (s.getOutput()) {
				positiveSamples.add(s.getInput());
			}
			else {
				negativeSamples.add(s.getInput());
			}
		}
	}

	@Override
	public M computeModel() {
		M result = getInitialModel();

		final Set<Integer> redStates = new HashSet<>();
		final Set<Integer> blueStates = new HashSet<>();

		redStates.add(result.getInitialState());

		for (final I i : result.getInputAlphabet()) {
			Integer rootSucc = result.getSuccessor(result.getInitialState(), i);
			if (rootSucc != null) {
				blueStates.add(rootSucc);
			}
		}

		final List<Future<Optional<D>>> searchTasks = new ArrayList<>(result.size());
		final Comparator<Integer> blueStateComparator = getBlueStateComparator(result);
		final Comparator<Integer> redStateComparator = getRedStateComparator(result);

		while (!blueStates.isEmpty()) {
			final Integer blueState = Collections.min(blueStates, blueStateComparator);
			final M finalCopyOfCurrentResult = result;

			blueStates.remove(blueState);

			final List<Integer> sortedRedStates = new ArrayList<>(redStates);
			Collections.sort(sortedRedStates, redStateComparator);

			// start multiple searches at once to improve performance
			for (final Integer redState : sortedRedStates) {
				searchTasks.add(threadPoolExecutor.submit(() -> computeMergedModel(finalCopyOfCurrentResult,
																				   redState,
																				   blueState)));
			}

			boolean foundMergeableStates = false;

			try {
				for (final Future<Optional<D>> f : searchTasks) {
					final Optional<D> temporaryResult = f.get();

					if (temporaryResult.isPresent()) {
						result = extractMergedModel(temporaryResult.get());
						foundMergeableStates = true;
						break;
					}
				}
			}
			catch (InterruptedException | ExecutionException ee) {
				// This should not happen
				throw new RuntimeException(ee);
			}
			finally {
				for (final Future<Optional<D>> f : searchTasks) {
					f.cancel(true);
				}
				searchTasks.clear();
			}

			if (foundMergeableStates) {
				for (final Integer red : redStates) {
					for (final I i : result.getInputAlphabet()) {
						final Integer redSuccessor = result.getSuccessor(red, i);

						if (redSuccessor != null && !redStates.contains(redSuccessor)) {
							blueStates.add(redSuccessor);
						}
					}
				}
			}
			else {
				RPNIUtil.promote(result, redStates, blueStates, blueState);
			}
		}

		updateStateAcceptance(result, negativeSamples, false);

		return result;
	}

	protected abstract M getInitialModel();

	protected abstract Optional<D> computeMergedModel(final M model, final Integer redState, final Integer blueState);

	protected abstract M extractMergedModel(final D data);

	protected Comparator<Integer> getRedStateComparator(final M model) {
		return Integer::compare;
	}

	protected Comparator<Integer> getBlueStateComparator(final M model) {
		return (i1, i2) -> 0;
	}

	protected void updateStateAcceptance(final M model, final Collection<Word<I>> traces, final boolean acceptance) {
		for (final Word<I> w : traces) {
			final Integer reachedState = model.getState(w);

			if (reachedState != null) {
				model.setAccepting(reachedState, acceptance);
			}
		}
	}
}
