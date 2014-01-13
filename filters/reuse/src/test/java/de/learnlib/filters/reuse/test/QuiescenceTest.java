/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.filters.reuse.test;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseOracle;

/**
 * Similar to the {@link LearningTest} but this time with quiescence. The purpose
 * of this test is just to check that the reuse filter is able to work with
 * <code>null</code> outputs.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class QuiescenceTest {
	private ReuseOracle<Integer, Integer, String> reuseOracle;
	private Alphabet<Integer> sigma;
	
	/**
	 * {@inheritDoc}.
	 */
	@BeforeClass
	protected void setUp() {
		ReuseCapableOracle<Integer, Integer, String> reuseCapableOracle = new TestOracle(
				3);
		sigma = Alphabets.integers(0, 3);
		reuseOracle = new ReuseOracle<>(sigma, reuseCapableOracle, true);
	}

	@Test
	public void simpleTest() {
		MealyLearner<Integer, String> learner = new ExtensibleLStarMealyBuilder<Integer, String>()
				.withAlphabet(sigma).withOracle(reuseOracle).create();

		learner.startLearning();
	}

	class TestOracle implements ReuseCapableOracle<Integer, Integer, String> {
		private int threshold;

		public TestOracle(int threshold) {
			this.threshold = threshold;
		}

		@Override
		public QueryResult<Integer, String> continueQuery(Word<Integer> trace,
				Integer s) {

			Integer integer = s;

			WordBuilder<String> output = new WordBuilder<>();
			for (Integer symbol : trace) {
				if (integer + symbol < threshold) {
					integer += symbol;
					output.add("ok");
				} else if (integer + symbol == threshold) {
					integer += symbol;
					output.add("done");
				} else {
					output.add(null); // quiescence
				}
			}

			QueryResult<Integer, String> result;
			result = new QueryResult<Integer, String>(output.toWord(), integer);

			return result;
		}

		@Override
		public QueryResult<Integer, String> processQuery(Word<Integer> trace) {
			Integer integer = new Integer(0);
			WordBuilder<String> output = new WordBuilder<>();
			for (Integer symbol : trace) {
				if (integer + symbol < threshold) {
					integer += symbol;
					output.add("ok");
				} else if (integer + symbol == threshold) {
					integer += symbol;
					output.add("done");
				} else {
					output.add(null); // quiescence
				}
			}

			QueryResult<Integer, String> result;
			result = new QueryResult<Integer, String>(output.toWord(), integer);

			return result;
		}
	}
}
