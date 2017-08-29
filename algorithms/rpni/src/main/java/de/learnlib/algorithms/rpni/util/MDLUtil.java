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

import de.learnlib.algorithms.rpni.automata.PTA;
import net.automatalib.words.Word;

import java.util.Collection;

/**
 * @author frohme
 */
public class MDLUtil {

	public static <I> double computeScore(final PTA<I> pta, final Collection<Word<I>> positiveSamples) {
		double sampleScore = 0;

		for (final Word<I> w : positiveSamples) {
			sampleScore += computeChoices(pta, w);
		}

		return pta.size() * pta.getInputAlphabet().size() + sampleScore;
	}

	private static <I> double computeChoices(final PTA<I> pta, final Word<I> word) {
		Integer currentState = pta.getInitialState();
		double result = Math.log(computeChoices(pta, currentState)) / Math.log(2); // log_2 x = log_e x / log_e 2

		for (final I i : word) {
			currentState = pta.getSuccessor(currentState, i);
			result += Math.log(computeChoices(pta, currentState)) / Math.log(2);
		}

		return result;
	}

	private static <I> int computeChoices(final PTA<I> pta, final Integer state) {
		int choices = (pta.isAccepting(state)) ? 1 : 0;

		for (final I i : pta.getInputAlphabet()) {
			if (pta.getSuccessor(state, i) != null) {
				choices++;
			}
		}

		return choices;
	}
}
