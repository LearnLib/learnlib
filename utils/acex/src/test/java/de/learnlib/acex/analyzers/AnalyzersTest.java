/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.acex.analyzers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import de.learnlib.acex.AbstractCounterexample;

@Test
public class AnalyzersTest {

	private static final int LENGTH = 100;
	
	private static final long SEED = 234253453253L;
	
	private static final int NUM_RANDOM = 10;
	
	private static AbstractCounterexample createOne0(int length) {
		int[] values = new int[length + 1];
		values[0] = 0;
		Arrays.fill(values, 1, values.length, 1);
		return new DummyAcex(values);
	}
	
	private static AbstractCounterexample createOne1(int length) {
		int[] values = new int[length + 1];
		values[length] = 1;
		Arrays.fill(values, 0, length, 0);
		return new DummyAcex(values);
	}
	
	private static AbstractCounterexample createRandom(int length, Random random) {
		int[] values = new int[length + 1];
		values[0] = 0;
		values[length] = 1;
		for (int i = 1; i < length; i++) {
			values[i] = random.nextInt(2);
		}
		return new DummyAcex(values);
	}
	
	@DataProvider(name = "analyzers")
	public NamedAcexAnalyzer[][] analyzers() {
		Collection<NamedAcexAnalyzer> analyzers = AcexAnalyzers.getAllAnalyzers();
		NamedAcexAnalyzer[][] result = new NamedAcexAnalyzer[analyzers.size()][1];
		int i = 0;
		for (NamedAcexAnalyzer a : analyzers) {
			result[i++][0] = a;
		}
		return result;
	}
	
	@Test(dataProvider = "analyzers")
	public void testOne0(NamedAcexAnalyzer analyzer) {
		AbstractCounterexample acex = createOne0(LENGTH);
		
		int idx = analyzer.analyzeAbstractCounterexample(acex);
		checkResult(acex, idx);
	}
	
	@Test(dataProvider = "analyzers")
	public void testOne1(NamedAcexAnalyzer analyzer) {
		AbstractCounterexample acex = createOne1(LENGTH);
		
		int idx = analyzer.analyzeAbstractCounterexample(acex);
		checkResult(acex, idx);
	}
	
	@Test(dataProvider = "analyzers")
	public void testRandom(NamedAcexAnalyzer analyzer) {
		Random r = new Random(SEED);
		
		for (int i = 0; i < NUM_RANDOM; i++) {
			AbstractCounterexample acex = createRandom(LENGTH, r);
		
			int idx = analyzer.analyzeAbstractCounterexample(acex);
			checkResult(acex, idx);
		}
	}
	
	private static void checkResult(AbstractCounterexample acex, int idx) {
		Assert.assertEquals(acex.test(idx), 0);
		Assert.assertEquals(acex.test(idx+1), 1);
	}
}
