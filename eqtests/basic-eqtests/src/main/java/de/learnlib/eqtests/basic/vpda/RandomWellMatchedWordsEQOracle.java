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
package de.learnlib.eqtests.basic.vpda;

import com.google.common.base.Preconditions;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

/**
 * An equivalence oracle based on the generation of random (well-matched) words.
 *
 * @param <I> input symbol type
 *
 * @author Malte Isberner
 */
public class RandomWellMatchedWordsEQOracle<I> implements EquivalenceOracle<OneSEVPA<?, I>, I, Boolean> {

	private final Random random;

	private final MembershipOracle<I, Boolean> oracle;

	private final VPDAlphabet<I> alphabet;

	private final double callProb;

	private final int maxTests, minLength, maxLength;

	public RandomWellMatchedWordsEQOracle(final Random random,
										  final MembershipOracle<I, Boolean> oracle,
										  final VPDAlphabet<I> alphabet,
										  final double callProb,
										  final int maxTests,
										  final int minLength,
										  final int maxLength) {
		Preconditions.checkArgument(minLength <= maxLength, "minLength is smaller than maxLength");

		this.random = random;
		this.oracle = oracle;
		this.alphabet = alphabet;
		this.callProb = callProb;
		this.maxTests = maxTests;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

	private Word<I> generateWellMatched(final int len) {
		WordBuilder<I> wb = new WordBuilder<>(len);
		generateWellMatched(wb, len);
		return wb.toWord();
	}

	private void generateWellMatched(WordBuilder<I> wb, int length) {
		if (length == 0) {
			return;
		}
		if (length == 1) {
			wb.append(alphabet.getInternalSymbol(random.nextInt(alphabet.getNumInternals())));
			return;
		}
		double act = random.nextDouble();
		if (act < callProb) {
			boolean dir = random.nextBoolean();
			if (dir) {
				final int cpos = random.nextInt(length - 1);
				generateWellMatched(wb, cpos);
				wb.append(alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())));
				final int rpos = cpos + 1 + random.nextInt(length - cpos - 1);
				generateWellMatched(wb, rpos - cpos - 1);
				wb.append(alphabet.getReturnSymbol(random.nextInt(alphabet.getNumReturns())));
				generateWellMatched(wb, length - rpos - 1);
			}
			else {
				final int rpos = 1 + random.nextInt(length - 1);
				final int cpos = random.nextInt(rpos);
				generateWellMatched(wb, cpos);
				wb.append(alphabet.getCallSymbol(random.nextInt(alphabet.getNumCalls())));
				generateWellMatched(wb, rpos - cpos - 1);
				wb.append(alphabet.getReturnSymbol(random.nextInt(alphabet.getNumReturns())));
				generateWellMatched(wb, length - rpos - 1);
			}
		}
		else {
			final int sep = 1 + random.nextInt(length - 1);
			generateWellMatched(wb, sep);
			generateWellMatched(wb, length - sep);
		}
	}

	@Override
	public DefaultQuery<I, Boolean> findCounterExample(final OneSEVPA<?, I> hypothesis,
													   final Collection<? extends I> inputs) {

		final int lengthRange = (maxLength - minLength) + 1;

		for (int i = 0; i < this.maxTests; i++) {
			final Word<I> queryWord = generateWellMatched(minLength + random.nextInt(lengthRange));
			final DefaultQuery<I, Boolean> query = new DefaultQuery<>(queryWord);

			final Boolean hypOutput = hypothesis.computeOutput(queryWord);
			oracle.processQuery(query);

			if (!Objects.equals(hypOutput, query.getOutput())) {
				return query;
			}
		}

		return null;
	}

}
