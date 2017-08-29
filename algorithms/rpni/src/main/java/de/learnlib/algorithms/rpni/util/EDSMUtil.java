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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author frohme
 */
public class EDSMUtil {

	public static <S, I> float count(final DFA<S, I> dfa,
									 final Collection<Word<I>> positiveSamples,
									 final Collection<Word<I>> negativeSamples) {

		final List<S> states = new ArrayList<>(dfa.getStates());

		final int[] tp = new int[states.size()];
		final int[] tn = new int[states.size()];

		for (final Word<I> w : positiveSamples) {
			int index = states.indexOf(dfa.getState(w));
			if (index > -1) {
				tp[index]++;
			}
		}

		for (final Word<I> w : negativeSamples) {
			int index = states.indexOf(dfa.getState(w));
			if (index > -1) {
				tn[index]++;
			}
		}

		int score = 0;

		for (final S s : states) {
			final int indexOfCurrentState = states.indexOf(s);
			if (tn[indexOfCurrentState] > 0) {
				if (tp[indexOfCurrentState] > 0) {
					return Float.NEGATIVE_INFINITY;
				}
				else {
					score += tn[indexOfCurrentState] - 1;
				}
			}
			else {
				if (tp[indexOfCurrentState] > 0) {
					score += tp[indexOfCurrentState] - 1;
				}
			}
		}

		return score;
	}
}
