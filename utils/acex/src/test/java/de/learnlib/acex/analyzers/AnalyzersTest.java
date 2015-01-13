/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
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
