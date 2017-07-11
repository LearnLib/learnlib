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

import de.learnlib.algorithms.rpni.MDLLearner;
import de.learnlib.algorithms.rpni.automata.PTA;
import net.automatalib.words.Word;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Examples taken from the book "Grammatical Inference" by de la Higuera.
 *
 * @author frohme
 */
public class MDLUtilTest {

	@Test
	public void testComputedValuesFromBook() {

		final Word<Character> p1 = Word.fromString("a");
		final Word<Character> p2 = Word.fromString("aa");
		final Word<Character> p3 = Word.fromString("bb");
		final Word<Character> p4 = Word.fromString("aaa");
		final Word<Character> p5 = Word.fromString("bba");
		final Word<Character> p6 = Word.fromString("aaaa");
		final Word<Character> p7 = Word.fromString("abba");
		final Word<Character> p8 = Word.fromString("bbbb");

		final List<Word<Character>> positiveSamples = Arrays.asList(p1, p2, p3, p4, p5, p6, p7, p8);

		final PTA<Character> pta = new PTA<>(PTAUtil.buildAlphabetFromSamples(positiveSamples, Collections.emptyList()));
		PTAUtil.fillWithSamples(pta, positiveSamples, Collections.emptyList());
		final double encodingInformation = MDLUtil.computeScore(pta, positiveSamples);

		Assert.assertEquals(pta.size(), 13);

		Assert.assertTrue(51.67 < encodingInformation);
		Assert.assertTrue(encodingInformation < 51.68);


		final MDLLearner<Character> learner = new MDLLearner<>();
		learner.addPositiveSamples(positiveSamples);

		final PTA<Character> result = learner.computeModel();
		final double finalEncodingInformation = MDLUtil.computeScore(result, positiveSamples);

		Assert.assertEquals(result.size(), 2);

		// the official value of the book (43.68) is wrong. if computed by hand the value should be around 45.21
		Assert.assertTrue(45.2 < finalEncodingInformation);
		Assert.assertTrue(finalEncodingInformation < 45.21);
	}
}
