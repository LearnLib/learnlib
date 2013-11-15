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

import net.automatalib.words.Word;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import de.learnlib.filters.reuse.api.InjectableSystemStateRef;
import de.learnlib.filters.reuse.ssrs.IntegerSystemStateRef;
import de.learnlib.filters.reuse.symbols.IntegerResetSymbol;
import de.learnlib.filters.reuse.symbols.IntegerSymbol;

/**
 * This class tests some internals from the {@link ReuseOracleImpl}. Note that
 * the methods {@link ReuseOracleImpl#analyzeQuery(Word)},
 * {@link ReuseOracleImpl#answerQuery(Word)},
 * {@link ReuseOracleImpl#executeFullQuery(Word)} and
 * {@link ReuseOracleImpl#executeSuffixFromQuery(Word)} should not be called
 * directly, those methods are only public due to unit testing.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class IntegerReuseOracleTest {
	private ReuseOracleImpl<IntegerSystemStateRef<IntegerSymbol, String>, IntegerSymbol, String> reuseOracle;

	/**
	 * {@inheritDoc}.
	 */
	@BeforeClass
	protected void setUp() {
		InjectableSystemStateRef<IntegerSystemStateRef<IntegerSymbol, String>, IntegerSymbol, String> op = null;
		op = new IntegerResetSymbol();

		ExecutableOracleImpl<IntegerSystemStateRef<IntegerSymbol, String>, IntegerSymbol, String> executableOracle = null;
		executableOracle = new ExecutableOracleImpl<>(op);

		reuseOracle = new ReuseOracleImpl<>(executableOracle);
	}

	@Test
	public void testAnalyzeQuery() {
		IntegerSymbol i1 = new IntegerSymbol(1);
		IntegerSymbol i2 = new IntegerSymbol(2);
		IntegerSymbol i3 = new IntegerSymbol(3);
		IntegerSymbol.UPPER_BOUND = 3;
		
		String ACK = "ACK";
		String NAK = "NAK";

		reuseOracle.getReuseTree().addFailureOutputSymbol(NAK);

		Word<IntegerSymbol> query = Word.fromSymbols(i1);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracleImpl.NeededAction.RESET_NECCESSARY);
		Word<String> output = reuseOracle.executeFullQuery(query); /* ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK));

		query = Word.fromSymbols(i2);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracleImpl.NeededAction.RESET_NECCESSARY);
		output = reuseOracle.executeFullQuery(query); /* ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK));
		
		query = Word.fromSymbols(i1, i3);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracleImpl.NeededAction.PREPARE_PREFIX);
		output = reuseOracle.executeSuffixFromQuery(query); /* ACK NAK */
		Assert.assertEquals(output, Word.fromSymbols(ACK, NAK));

		query = Word.fromSymbols(i1, i2);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracleImpl.NeededAction.PREPARE_PREFIX);
		output = reuseOracle.executeSuffixFromQuery(query); /* ACK ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK, ACK));

		// should be already known, pump reflexive edge
		query = Word.fromSymbols(i1, i3, i3, i2);
		Assert.assertTrue(reuseOracle.analyzeQuery(query) == ReuseOracleImpl.NeededAction.ALREADY_KNOWN);
		output = reuseOracle.answerQuery(query); /* ACK NAK NAK ACK */
		Assert.assertEquals(output, Word.fromSymbols(ACK, NAK, NAK, ACK));
	}
}
