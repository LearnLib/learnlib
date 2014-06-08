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

import java.io.IOException;

import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;

import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseOracle;
import de.learnlib.filters.reuse.ReuseOracle.ReuseOracleBuilder;
import de.learnlib.filters.reuse.tree.ReuseTree;

/**
 * Simple learning test that shows who to use the reuse oracle.
 *  
 * @author Oliver Bauer 
 */
public class LearningTest {
	private ReuseOracle<Integer, Integer, String> reuseOracle;
	private Alphabet<Integer> sigma;
	
	/**
	 * {@inheritDoc}.
	 */
	@BeforeClass
	protected void setUp() {
		sigma = Alphabets.integers(0, 3);

		reuseOracle = new ReuseOracleBuilder<>(sigma, new TestOracleFactory())
				.withFailureOutputs(Sets.newHashSet("error"))
				.withInvariantInputs(Sets.newHashSet(0))
				.build();
	}

	@Test
	public void simpleTest() throws IOException {

		MealyLearner<Integer, String> learner = new ExtensibleLStarMealyBuilder<Integer, String>()
				.withAlphabet(sigma).withOracle(reuseOracle).create();

		learner.startLearning();
		
		ReuseTree<Integer, Integer, String> reuseTree = reuseOracle.getReuseTree();
		
		Appendable sb = new StringBuffer();
		GraphDOT.write(reuseTree, reuseTree.getGraphDOTHelper(), sb);
		Assert.assertTrue(sb.toString().startsWith("digraph g"));
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
				if (integer + symbol <= threshold) {
					integer += symbol;
					output.add("ok");
				} else {
					output.add("error");
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
				if (integer + symbol <= threshold) {
					integer += symbol;
					output.add("ok");
				} else {
					output.add("error");
				}
			}

			QueryResult<Integer, String> result;
			result = new QueryResult<>(output.toWord(), integer);

			return result;
		}
	}
}