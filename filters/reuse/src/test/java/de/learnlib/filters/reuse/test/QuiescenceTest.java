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

import com.google.common.base.Supplier;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseOracle;
import de.learnlib.filters.reuse.ReuseOracle.ReuseOracleBuilder;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Similar to the {@link LearningTest} but this time with quiescence in outputs. 
 * The purpose of this test is just to check that the reuse filter is able to work with
 * {@code null} outputs.
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
		sigma = Alphabets.integers(0, 3);
		reuseOracle = new ReuseOracleBuilder<>(sigma, new TestOracleFactory()).build();
	}

	@Test
	public void simpleTest() {
		MealyLearner<Integer, String> learner = new ExtensibleLStarMealyBuilder<Integer, String>()
				.withAlphabet(sigma).withOracle(reuseOracle).create();

		learner.startLearning();
	}

	private class TestOracleFactory implements Supplier<ReuseCapableOracle<Integer, Integer, String>> {

		@Override
		public ReuseCapableOracle<Integer, Integer, String> get() {
			return new TestOracle();
		}

	}

	class TestOracle implements ReuseCapableOracle<Integer, Integer, String> {
		private final int threshold = 3;

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
			result = new QueryResult<>(output.toWord(), integer);

			return result;
		}

		@Override
		public QueryResult<Integer, String> processQuery(Word<Integer> trace) {
			Integer integer = 0;
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
			result = new QueryResult<>(output.toWord(), integer);

			return result;
		}
	}
}
