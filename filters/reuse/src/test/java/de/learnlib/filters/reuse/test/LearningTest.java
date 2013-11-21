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

import static de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers.SHAHBAZ;
import static de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies.CLOSE_FIRST;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseOracle;
import de.learnlib.filters.reuse.tree.ReuseNode;

public class LearningTest {
	private ReuseOracle<Integer, Integer, String> reuseOracle;

	/**
	 * {@inheritDoc}.
	 */
	@BeforeClass
	protected void setUp() {
		ReuseCapableOracle<Integer, Integer, String> reuseCapableOracle = new TestOracle(
				3);
		reuseOracle = new ReuseOracle<>(reuseCapableOracle);
	}

	@Test
	public void simpleTest() {
		Alphabet<Integer> sigma = Alphabets.integers(0, 3);

		ExtensibleLStarMealy<Integer, String> learner = createLearner(sigma,
				reuseOracle);
		learner.startLearning();
	}

	private ExtensibleLStarMealy<Integer, String> createLearner(
			Alphabet<Integer> sigma,
			MealyMembershipOracle<Integer, String> oracle) {

		// TODO Use Factory
		final ExtensibleLStarMealy<Integer, String> learner;

		ObservationTableCEXHandler<Object, Object> cex = SHAHBAZ;
		cex = ObservationTableCEXHandlers.RIVEST_SCHAPIRE;
		cex = ObservationTableCEXHandlers.SUFFIX1BY1;

		ClosingStrategy<Object, Object> cs = CLOSE_FIRST;

		List<Word<Integer>> suffixes = new ArrayList<>();// Collections.emptyList();
		for (int i = 0; i <= sigma.size() - 1; i++) {
			Word<Integer> w = Word.fromLetter(sigma.getSymbol(i));
			suffixes.add(w);
		}

		learner = new ExtensibleLStarMealy<>(sigma, oracle, suffixes, cex, cs);

		return learner;
	}

	class TestOracle implements ReuseCapableOracle<Integer, Integer, String> {
		private int threshold;

		public TestOracle(int threshold) {
			this.threshold = threshold;
		}

		@Override
		public QueryResult<Integer, String> continueQuery(Word<Integer> trace,
				ReuseNode<Integer, Integer, String> s) {

			Integer integer = s.getSystemState();

			WordBuilder<String> output = new WordBuilder<>();
			for (Integer symbol : trace) {
				if (integer + symbol <= threshold) {
					integer += symbol;
					output.add("ok");
				} else {
					output.add("error");
				}
			}

			QueryResult<Integer, String> result = new QueryResult<Integer, String>(
					output.toWord(), integer, true);

			return result;
		}

		@Override
		public QueryResult<Integer, String> processQuery(Word<Integer> trace) {

			Integer integer = new Integer(0);
			WordBuilder<String> output = new WordBuilder<>();
			for (Integer symbol : trace) {
				if (integer + symbol <= threshold) {
					integer += symbol;
					output.add("ok");
				} else {
					output.add("error");
				}
			}

			QueryResult<Integer, String> result = new QueryResult<Integer, String>(
					output.toWord(), integer, true);

			return result;
		}
	}
}