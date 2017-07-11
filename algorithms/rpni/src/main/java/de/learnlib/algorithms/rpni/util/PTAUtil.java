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
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author frohme
 */
public class PTAUtil {

	public static <I> Alphabet<I> buildAlphabetFromSamples(final Collection<Word<I>> positiveSamples,
														   final Collection<Word<I>> negativeSamples) {

		final Set<I> inputSymbols = Stream.concat(positiveSamples.stream(), negativeSamples.stream())
				.flatMap(Word::stream)
				.collect(Collectors.toSet());

		return Alphabets.fromCollection(inputSymbols);
	}

	public static <I> void fillWithSamples(final PTA<I> pta,
										   final Collection<Word<I>> positiveSamples,
										   final Collection<Word<I>> negativeSamples) {
		fillWithSamples(pta, positiveSamples, negativeSamples, a -> {});
	}

	public static <I> void fillWithSamples(final CPTA<I> cpta,
										   final Collection<Word<I>> positiveSamples,
										   final Collection<Word<I>> negativeSamples) {
		fillWithSamples(cpta, positiveSamples, negativeSamples, s -> cpta.setCoverage(s, cpta.getCoverage(s) + 1));
	}

	private static <I> void fillWithSamples(final PTA<I> pta,
											final Collection<Word<I>> positiveSamples,
											final Collection<Word<I>> negativeSamples,
											final Consumer<Integer> preProcessing) {

		final Integer start = pta.addInitialState(false);

		for (final Word<I> w : positiveSamples) {
			insertTrace(pta, start, w.iterator(), true, preProcessing);
		}

		for (final Word<I> w : negativeSamples) {
			insertTrace(pta, start, w.iterator(), false, preProcessing);
		}
	}

	private static <I> void insertTrace(final PTA<I> pta,
										final Integer currentState,
										final Iterator<I> inputIterator,
										final boolean acceptLastState,
										final Consumer<Integer> preProcessing) {

		preProcessing.accept(currentState);

		if (!inputIterator.hasNext()) {
			pta.setAccepting(currentState, acceptLastState);
		}
		else {
			final I nextInput = inputIterator.next();
			final Integer nextState = pta.getSuccessor(currentState, nextInput);

			if (nextState == null) {
				final Integer newState = pta.addState(false);

				pta.addTransition(currentState, nextInput, newState);

				pta.setParentNode(newState, currentState);
				pta.setParentNodeInput(newState, nextInput);

				insertTrace(pta, newState, inputIterator, acceptLastState, preProcessing);
			}
			else {
				insertTrace(pta, nextState, inputIterator, acceptLastState, preProcessing);
			}
		}
	}
}
