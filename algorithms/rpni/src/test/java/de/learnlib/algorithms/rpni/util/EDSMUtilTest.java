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
public class EDSMUtilTest {

	@Test
	public void testEDSMCount() {

		final Word<Character> p1 = Word.fromString("a");
		final Word<Character> p2 = Word.fromString("aaa");
		final Word<Character> p3 = Word.fromString("bba");
		final Word<Character> p4 = Word.fromString("abab");

		final Word<Character> n1 = Word.fromString("ab");
		final Word<Character> n2 = Word.fromString("bb");

		final List<Word<Character>> positiveSamples = Arrays.asList(p1, p2, p3, p4);
		final List<Word<Character>> negativeSamples = Arrays.asList(n1, n2);

		// constructing pta from fig 14.11 p. 295
		final PTA<Character> result = new PTA<>(PTAUtil.buildAlphabetFromSamples(positiveSamples, negativeSamples));
		PTAUtil.fillWithSamples(result, Arrays.asList(p1, Word.fromString("aba"), p3, p4), Collections.emptySet());

		result.addTransition(result.getState(p1), 'a', result.getInitialState());

		final PTA<Character> e_ab = RPNIUtil.merge(result, result.getInitialState(), result.getState(n1));
		final PTA<Character> a_ab = RPNIUtil.merge(result, result.getState(p1), result.getState(n1));

		final PTA<Character> e_b =
				RPNIUtil.merge(result, result.getInitialState(), result.getState(Word.fromString("b")));
		final PTA<Character> a_b = RPNIUtil.merge(result, result.getState(p1), result.getState(Word.fromString("b")));

		Assert.assertEquals(Float.NEGATIVE_INFINITY, EDSMUtil.count(e_ab, positiveSamples, negativeSamples));
		Assert.assertEquals(Float.NEGATIVE_INFINITY, EDSMUtil.count(a_ab, positiveSamples, negativeSamples));

		Assert.assertEquals(2F, EDSMUtil.count(e_b, positiveSamples, negativeSamples));
		// book is wrong, should be 2
		Assert.assertEquals(2F, EDSMUtil.count(a_b, positiveSamples, negativeSamples));
	}
}
