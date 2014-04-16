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
import net.automatalib.words.impl.Alphabets;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;

import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filters.reuse.ReuseOracle;
import de.learnlib.filters.reuse.ReuseOracle.ReuseOracleBuilder;
import de.learnlib.filters.reuse.tree.ReuseNode.NodeResult;

/**
 * Reuse oracle test that uses invariant input symbols.
 *  
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public class DomainKnowledgeTest {
	private ReuseOracle<Integer, Integer, String> reuseOracle;

	private class NullReuseCapableFactory implements Supplier<ReuseCapableOracle<Integer, Integer, String>> {

		@Override
		public ReuseCapableOracle<Integer, Integer, String> get() {
			return new ReuseCapableOracle<Integer, Integer, String>() {
				@Override
				public QueryResult<Integer, String> continueQuery(Word<Integer> trace, Integer integer) {
					return null;
				}

				@Override
				public QueryResult<Integer, String> processQuery(Word<Integer> trace) {
					return null;
				}
			};
		}
	}

	/**
	 * {@inheritDoc}.
	 */
	@BeforeMethod
	protected void setUp() {
		// We don't use this oracle, we directly test against the reuse tree!
		Alphabet<Integer> alphabet = Alphabets.integers(0, 10);
		
		reuseOracle = new ReuseOracleBuilder<>(alphabet, new NullReuseCapableFactory())
				.withInvariantInputs(Sets.newHashSet(0))
				.build();
	}
	
	@Test
	public void testPumpSymbolsSimple() {
		// Add one entry (0,ok) where 0 is model invariant (reflexive edge)
		QueryResult<Integer, String> qr = new QueryResult<>(getOutput("ok"), 0);
		reuseOracle.getReuseTree().insert(getInput(0), qr);

		Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(0,0,0,0));
		Assert.assertNotNull(known);
		Assert.assertEquals(known.size(), 4);
		Assert.assertEquals(known, getOutput("ok","ok","ok","ok"));
	}
	
	@Test(dependsOnMethods={"testPumpSymbolsSimple"})
	public void testPumpSymbolsComplex() {
		// Add one entry (101,ok1 ok0 ok1) where 0 is model invariant
		QueryResult<Integer, String> qr = new QueryResult<>(getOutput("ok1","ok0","ok1"), 2);
		reuseOracle.getReuseTree().insert(getInput(1,0,1), qr);
		
		Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(1,0,0,0,0,1));
		Assert.assertNotNull(known);
		Assert.assertEquals(known.size(), 6);
		Assert.assertEquals(known, getOutput("ok1","ok0","ok0","ok0","ok0","ok1"));
	}

	@Test
	public void testReuseNodePrefixWhileReusing() {
		QueryResult<Integer, String> qr = new QueryResult<>(getOutput("ok","ok","ok"), 2);
		reuseOracle.getReuseTree().insert(getInput(1, 0, 1), qr);
		
		Word<Integer> input = getInput(1,0,1,1);
		NodeResult<Integer, Integer, String> node = reuseOracle.getReuseTree().fetchSystemState(input);

		Assert.assertTrue(node.prefixLength == 3); // ''1 0 1''
		// reuse the prefix
		qr = new QueryResult<>(getOutput("ok"), 3);
		reuseOracle.getReuseTree().insert(getInput(1), node.reuseNode, qr);
		
		// The "1 1" system state should not be available:
		node = reuseOracle.getReuseTree().fetchSystemState(getInput(1,1));
		Assert.assertNull(node);
		
		// There should be a "1 1 1" system state, even this query was never seen
		node = reuseOracle.getReuseTree().fetchSystemState(getInput(1,1,1));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 3); // query "1 1 1"
		
		// The system state is invalidated, so querying again "1 1 1" reveals null this time
		node = reuseOracle.getReuseTree().fetchSystemState(getInput(1,1,1));
		Assert.assertNull(node);
		
		// The output of "1 0 0 0 0 1 1" should be known, even the query was never seen
		Word<String> output = reuseOracle.getReuseTree().getOutput(getInput(1,0,0,0,0,1,1));
		Assert.assertNotNull(output);
		Assert.assertTrue(output.size() == 7);
	}
	
	private static Word<Integer> getInput(Integer... param){
		return Word.fromSymbols(param);
	}
	
	private static Word<String> getOutput(String... param){
		return Word.fromSymbols(param);
	}
	
}
