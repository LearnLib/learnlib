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

import de.learnlib.algorithms.rpni.util.RPNIUtil;
import de.learnlib.algorithms.rpni.automata.PTA;
import de.learnlib.algorithms.rpni.util.PTAUtil;

import java.util.Collections;
import java.util.Optional;

/**
 * "Regular positive-negative inference" algorithm. Implementation based on the book "Grammatical Inference" by
 * de la Higuera.
 *
 * @param <I> input alphabet type
 *
 * @author frohme
 */
public class RPNILearner<I> extends AbstractStateMergingLearner<I, PTA<I>, PTA<I>> {

	@Override
	protected PTA<I> getInitialModel() {
		final PTA<I> result = new PTA<>(PTAUtil.buildAlphabetFromSamples(super.positiveSamples, super.negativeSamples));
		PTAUtil.fillWithSamples(result, super.positiveSamples, Collections.emptyList());

		return result;
	}

	@Override
	protected Optional<PTA<I>> computeMergedModel(PTA<I> model, Integer redState, Integer blueState) {
		final PTA<I> mergedAutomaton = RPNIUtil.merge(model, redState, blueState);

		if (RPNIUtil.isCompatible(mergedAutomaton, negativeSamples)) {
			return Optional.of(mergedAutomaton);
		}

		return Optional.empty();
	}

	@Override
	protected PTA<I> extractMergedModel(PTA<I> data) {
		return data;
	}
}
