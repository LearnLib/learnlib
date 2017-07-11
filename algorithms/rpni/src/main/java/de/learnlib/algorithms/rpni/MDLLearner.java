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

import de.learnlib.algorithms.rpni.util.MDLUtil;
import de.learnlib.algorithms.rpni.util.RPNIUtil;
import de.learnlib.algorithms.rpni.automata.PTA;
import de.learnlib.algorithms.rpni.util.PTAUtil;

import java.util.Collections;
import java.util.Optional;

/**
 * "Minimum description length" heuristic for the RPNI algorithm. Implementation based on the book
 * "Grammatical Inference" by de la Higuera.
 *
 * @param <I> input alphabet type
 *
 * @author frohme
 */
public class MDLLearner<I> extends AbstractStateMergingLearner<I, PTA<I>, MDLLearner.MDLComputationResult<I>> {

	static class MDLComputationResult<I> {

		final PTA<I> pta;

		final double score;

		public MDLComputationResult(final PTA<I> pta, final double score) {
			this.pta = pta;
			this.score = score;
		}

		public PTA<I> getPta() {
			return this.pta;
		}

		public double getScore() {
			return this.score;
		}

	}

	private double currentMDLScore;

	@Override
	protected PTA<I> getInitialModel() {
		final PTA<I> result = new PTA<>(PTAUtil.buildAlphabetFromSamples(positiveSamples, Collections.emptySet()));
		PTAUtil.fillWithSamples(result, positiveSamples, Collections.emptySet());

		return result;
	}

	@Override
	public PTA<I> computeModel() {
		this.currentMDLScore = Double.POSITIVE_INFINITY;
		return super.computeModel();
	}

	@Override
	protected Optional<MDLComputationResult<I>> computeMergedModel(PTA<I> model, Integer redState, Integer blueState) {
		final PTA<I> mergedAutomaton = RPNIUtil.merge(model, redState, blueState);
		final double newScore = MDLUtil.computeScore(mergedAutomaton, super.positiveSamples);

		if (newScore < currentMDLScore && RPNIUtil.isCompatible(mergedAutomaton, super.negativeSamples)) {
			return Optional.of(new MDLComputationResult<>(mergedAutomaton, newScore));
		}

		return Optional.empty();
	}

	@Override
	protected PTA<I> extractMergedModel(MDLComputationResult<I> data) {
		this.currentMDLScore = data.getScore();
		return data.getPta();
	}

}
