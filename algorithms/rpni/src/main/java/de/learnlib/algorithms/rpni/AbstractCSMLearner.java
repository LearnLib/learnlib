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

import de.learnlib.algorithms.rpni.util.PTAUtil;
import de.learnlib.algorithms.rpni.util.RPNIUtil;
import de.learnlib.algorithms.rpni.automata.CPTA;

import java.util.Collections;
import java.util.Optional;

/**
 * Abstract super class for merging heuristics that include the coverage of states for finding pairs of states to merge.
 * Implementation based on the book "Grammatical Inference" by de la Higuera.
 *
 * @param <I> input alphabet type
 *
 * @author frohme
 */
public abstract class AbstractCSMLearner<I> extends AbstractStateMergingLearner<I, CPTA<I>, CPTA<I>> {

	@Override
	protected CPTA<I> getInitialModel() {
		final CPTA<I> result = new CPTA<>(PTAUtil.buildAlphabetFromSamples(positiveSamples, negativeSamples));
		PTAUtil.fillWithSamples(result, positiveSamples, Collections.emptyList());

		return result;
	}

	@Override
	protected Optional<CPTA<I>> computeMergedModel(final CPTA<I> model, final Integer redState, final Integer blueState) {
		final CPTA<I> mergedAutomaton = RPNIUtil.merge(model, redState, blueState);

		if (RPNIUtil.isCompatible(mergedAutomaton, negativeSamples)) {
			return Optional.of(mergedAutomaton);
		}

		return Optional.empty();
	}

	@Override
	protected CPTA<I> extractMergedModel(final CPTA<I> data) {
		return data;
	}
}
