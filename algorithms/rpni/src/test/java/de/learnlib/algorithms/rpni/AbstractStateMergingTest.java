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

import de.learnlib.passive.api.PassiveDFALearner;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author frohme
 */
public abstract class AbstractStateMergingTest {

	private static final Random RANDOM = new Random(42);

	protected static final int DEFAULT_RANDOM_LENGTH = 5;
	protected static final int DEFAULT_RANDOM_SIZE = 5;

	protected <I> void testLearner(final PassiveDFALearner<I> learner,
								   final Collection<Word<I>> positiveSamples,
								   final Collection<Word<I>> negativeSamples) {

		learner.addPositiveSamples(positiveSamples);
		learner.addNegativeSamples(negativeSamples);

		final DFA<?, I> result = learner.computeModel();

		Assert.assertNotNull(result);

		positiveSamples.stream().map(result::accepts).forEach(Assert::assertTrue);
		negativeSamples.stream().map(result::accepts).forEach(Assert::assertFalse);
	}

	protected Word<Integer> generateRandomSample(final int length, final int maxSymbol, final boolean accepting) {
		final WordBuilder<Integer> wb = new WordBuilder<>(length + 1);

		// don't care about rounding
		for (int i = 0; i < length / 2; i++) {
			wb.append(RANDOM.nextInt(maxSymbol));
		}

		wb.append(accepting ? 1 : 0);

		for (int i = 0; i < length / 2; i++) {
			wb.append(RANDOM.nextInt(maxSymbol));
		}

		return wb.toWord();
	}

	protected List<Word<Integer>> generateRandomSamples(final int amount, final boolean accepting) {
		final List<Word<Integer>> result = new ArrayList<>(amount);

		for (int i = 0; i < amount; i++) {
			result.add(generateRandomSample(DEFAULT_RANDOM_LENGTH, DEFAULT_RANDOM_SIZE, accepting));
		}

		return result;
	}

}
