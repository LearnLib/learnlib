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

import net.automatalib.words.Word;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Examples partly taken from the book "Grammatical Inference" by de la Higuera.
 *
 * @author frohme
 */
public class RPNITest extends AbstractStateMergingTest {

	@Test
	public void testSampleFromBook() {

		final Word<Character> p1 = Word.fromString("aaa");
		final Word<Character> p2 = Word.fromString("aaba");
		final Word<Character> p3 = Word.fromString("bba");
		final Word<Character> p4 = Word.fromString("bbaba");

		final Word<Character> n1 = Word.fromString("a");
		final Word<Character> n2 = Word.fromString("bb");
		final Word<Character> n3 = Word.fromString("aab");
		final Word<Character> n4 = Word.fromString("aba");

		final List<Word<Character>> positiveSamples = Arrays.asList(p1, p2, p3, p4);
		final List<Word<Character>> negativeSamples = Arrays.asList(n1, n2, n3, n4);

		super.testLearner(new RPNILearner<>(), positiveSamples, negativeSamples);
	}

	@Test
	public void testRandomSample() {
		final List<Word<Integer>> positiveSamples = super.generateRandomSamples(4, true);
		final List<Word<Integer>> negativeSamples = super.generateRandomSamples(4, false);

		super.testLearner(new RPNILearner<>(), positiveSamples, negativeSamples);
	}

}
