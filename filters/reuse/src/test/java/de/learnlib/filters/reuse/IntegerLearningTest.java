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
package de.learnlib.filters.reuse;

import static de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers.SHAHBAZ;
import static de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies.CLOSE_FIRST;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategy;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.ssrs.IntegerSystemStateRef;
import de.learnlib.filters.reuse.symbols.IntegerResetSymbol;
import de.learnlib.filters.reuse.symbols.IntegerSymbol;

public class IntegerLearningTest {
	private ReuseOracleImpl<IntegerSystemStateRef<IntegerSymbol, String>, IntegerSymbol, String> reuseOracle;

	/**
	 * {@inheritDoc}.
	 */
	@BeforeClass
	protected void setUp() {
		InjectableSystemStateRef<
			IntegerSystemStateRef<IntegerSymbol, String>, 
			IntegerSymbol, 
			String
		> reset = null;
		reset = new IntegerResetSymbol();

		ExecutableOracleImpl<
			IntegerSystemStateRef<IntegerSymbol, String>,
			IntegerSymbol,
			String
		> executableOracle = null;
		executableOracle = new ExecutableOracleImpl<>(reset);
		
		reuseOracle = new ReuseOracleImpl<>(executableOracle);
	}
	
	@Test
	public void simpleTest() {
		reuseOracle.getReuseTree().addFailureOutputSymbol("NAK 6");
		reuseOracle.getReuseTree().addFailureOutputSymbol("NAK 7");
		reuseOracle.getReuseTree().useFailureOutputKnowledge(true);
		
		Alphabet<IntegerSymbol> sigma = new SimpleAlphabet<>();
		IntegerSymbol.UPPER_BOUND = 10; // get a hyp with one state, 6 mqs
		sigma.add(new IntegerSymbol(1));
		sigma.add(new IntegerSymbol(2));
		
		ExtensibleLStarMealy<IntegerSymbol, String> learner = createLearner(sigma, reuseOracle);
		learner.startLearning();
		
		Assert.assertTrue(reuseOracle.getAnswers() == 0);
		Assert.assertTrue(reuseOracle.getFull()    == 4);
		Assert.assertTrue(reuseOracle.getReuse()   == 2);
	}
	
	private ExtensibleLStarMealy<IntegerSymbol, String> createLearner(
			Alphabet<IntegerSymbol> sigma,
			MealyMembershipOracle<IntegerSymbol, String> oracle) {
		final ExtensibleLStarMealy<IntegerSymbol, String> learner;

		ObservationTableCEXHandler<Object, Object> cex = SHAHBAZ;
		cex = ObservationTableCEXHandlers.RIVEST_SCHAPIRE;
		cex = ObservationTableCEXHandlers.SUFFIX1BY1;

		ClosingStrategy<Object, Object> cs = CLOSE_FIRST;

		List<Word<IntegerSymbol>> suffixes = new ArrayList<>();// Collections.emptyList();
		for (int i = 0; i <= sigma.size() - 1; i++) {
			Word<IntegerSymbol> w = Word.fromLetter(sigma.getSymbol(i));
			suffixes.add(w);
		}

		learner = new ExtensibleLStarMealy<>(sigma, oracle, suffixes, cex, cs);

		return learner;
	}
}
