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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import de.learnlib.filters.reuse.ReuseCapableOracle;
import de.learnlib.filters.reuse.ReuseCapableOracle.QueryResult;
import de.learnlib.filters.reuse.ReuseOracle;
import de.learnlib.filters.reuse.tree.ReuseNode.NodeResult;

public class ReuseOracleTest {
	private ReuseOracle<Integer, Integer, String> reuseOracle;

	/**
	 * {@inheritDoc}.
	 */
	@BeforeMethod
	protected void setUp() {
		// We don't use this oracle, we directly test against the reuse tree!
		ReuseCapableOracle<Integer, Integer, String> reuseCapableOracle = new ReuseCapableOracle<Integer, Integer, String>() {

			@Override
			public QueryResult<Integer, String> continueQuery(
					Word<Integer> trace, Integer s) {
				return null;
			}

			@Override
			public QueryResult<Integer, String> processQuery(
					Word<Integer> trace) {
				return null;
			}
		};
		
		Alphabet<Integer> alphabet = Alphabets.integers(0, 10);
		reuseOracle = new ReuseOracle<>(alphabet, reuseCapableOracle);
	}
	
	@Test
	public void testTreeIsEmpty() {
		NodeResult<Integer, Integer, String> node = null;

		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(0));
		Assert.assertNull(node);
		
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1));
		Assert.assertNull(node);

		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(2));
		Assert.assertNull(node);
	}
	
	@Test(dependsOnMethods={"testTreeIsEmpty"})
	public void testTreeIsAbleToCache() {
		// Add one entry (1,ok)
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok"), 1, true);
		reuseOracle.getReuseTree().insert(getInput(1), qr);
		
		// check that query (1) is already known and has same output than before
		Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(1));
		Assert.assertNotNull(known);
		Assert.assertEquals(known, getOutput("ok"));
	}
	
	@Test(dependsOnMethods={"testTreeIsAbleToCache"})
	public void testTreeIsAbleToPumpModelInvariantSymbolsSimple() {
		reuseOracle.getReuseTree().addInvariantInputSymbol(0);
		reuseOracle.getReuseTree().useModelInvariantSymbols(true);
		
		// Add one entry (0,ok) where 0 is model invariant (reflexive edge)
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok"), 0, true);
		reuseOracle.getReuseTree().insert(getInput(0), qr);
		
		Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(0,0,0,0));
		Assert.assertNotNull(known);
		Assert.assertEquals(known.size(), 4);
		Assert.assertEquals(known, getOutput("ok","ok","ok","ok"));
	}
	
	@Test(dependsOnMethods={"testTreeIsAbleToPumpModelInvariantSymbolsSimple"})
	public void testTreeIsAbleToPumpModelInvariantSymbolsComplex() {
		reuseOracle.getReuseTree().addInvariantInputSymbol(0);
		reuseOracle.getReuseTree().useModelInvariantSymbols(true);
		
		// Add one entry (101,ok1 ok0 ok1) where 0 is model invariant
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok1","ok0","ok1"), 2, true);
		reuseOracle.getReuseTree().insert(getInput(1,0,1), qr);
		
		Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(1,0,0,0,0,1));
		Assert.assertNotNull(known);
		Assert.assertEquals(known.size(), 6);
		Assert.assertEquals(known, getOutput("ok1","ok0","ok0","ok0","ok0","ok1"));
	}
	
	@Test(dependsOnMethods={
			"testTreeIsAbleToPumpModelInvariantSymbolsSimple",
			"testTreeIsAbleToPumpModelInvariantSymbolsComplex"})
	public void testTreeDoesNotPump() {
		// Add one entry (0,ok) 
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok"), 0, true);
		reuseOracle.getReuseTree().insert(getInput(0), qr);
		
		Word<String> known = reuseOracle.getReuseTree().getOutput(getInput(0,0,0,0));
		// no model invariant input was defined...
		Assert.assertNull(known);
	}
	
	@Test
	public void testNoReusePossible() {
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok","ok"), 2, true);
		reuseOracle.getReuseTree().insert(getInput(1, 1), qr);
		
		/**
		 * Should result in
		 * <pre>
		 *  o
		 *  | 1/ok
		 *  o
		 *  | 1/ok
		 *  *
		 * </pre>
		 */
		
		// now we use query 12, no reuse possible
		NodeResult<Integer, Integer, String> node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,2));
		Assert.assertNull(node);
	}
	
	@Test(dependsOnMethods = {"testNoReusePossible"})
	public void testReusePossibleWithInvalidation() {
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok","ok"), 2, true);
		reuseOracle.getReuseTree().insert(getInput(1, 1), qr);
		
		/**
		 * Should result in
		 * <pre>
		 *  o
		 *  | 1/ok
		 *  o
		 *  | 1/ok
		 *  *
		 * </pre>
		 */
		
		// now we check query 112, reuse possible in 11
		NodeResult<Integer, Integer, String> node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,2));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 2); // query '1 1'

		qr = new QueryResult<Integer, String>(getOutput("ok"), 4, true);
		reuseOracle.getReuseTree().insert(getInput(2), node.s, qr);
		
		/**
		 * Should result in
		 * <pre>
		 *  o
		 *  | 1/ok
		 *  o
		 *  | 1/ok
		 *  o
		 *  | 2/ok
		 *  *
		 * </pre>
		 */
		
		// we check that 113 has no reusable prefix, since we invalidated the last system state:
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,3));
		Assert.assertNull(node);

		// but 1123 should have a reusable prefix via the new 112
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,2,3));
		Assert.assertNotNull(node);
	}

	@Test(dependsOnMethods = {"testReusePossibleWithInvalidation"})
	public void testReusePossibleWithoutInvalidation() {
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok","ok"), 2, true);
		reuseOracle.getReuseTree().insert(getInput(1, 1), qr);
		
		/**
		 * <pre>
		 * Should result in
		 *  o
		 *  | 1/ok
		 *  o
		 *  | 1/ok
		 *  *
		 * </pre>
		 */
		
		// now we check query 112, reuse possible in 11
		NodeResult<Integer, Integer, String> node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,2));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 2); // query "1 1"

		// now we dont't(!) invalidate the system state, so in "1 1" there remains "2"
		qr = new QueryResult<Integer, String>(getOutput("ok"), 4, false);
		reuseOracle.getReuseTree().insert(getInput(2), node.s, qr);
		
		/**
		 * Should result in
		 * <pre>
		 *  o
		 *  | 1/ok
		 *  o
		 *  | 1/ok
		 *  *
		 *  | 2/ok
		 *  *
		 * </pre>
		 */
		
		// we check that "1 1 3" has reusable prefix, since we have not invalidated the system state in "1 1":
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,3));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 2); // query "1 1"

		// but "1 1 2 3" should although have a reusable prefix via the new "1 1 2"
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,2,3));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 3); // query "1 1 2"
		
		// we query "1 1 4" and invalidate the system state in "1 1"
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,4));
		Assert.assertNotNull(node);

		qr = new QueryResult<Integer, String>(getOutput("ok"), 6, true);
		reuseOracle.getReuseTree().insert(getInput(4), node.s, qr);
		
		/**
		 * Should result in
		 * <pre>
		 *       o
		 *       | 1/ok
		 *       o
		 *       | 1/ok
		 *  -----o-----
		 *  | 4/ok    | 2/ok
		 *  *         *
		 * </pre>
		 */		
		
		// now "1 1 3" should have no reusable system state
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,3));
		Assert.assertNull(node);
		
		// now "1 1 4 1" should 
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,4,1));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 3); // query "1 1 4"

		// now "1 1 2 8" should 
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,2,8));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 3); // query "1 1 2"
	}
	
	@Test(
			expectedExceptions= {RuntimeException.class},
			dependsOnMethods={"testReusePossibleWithoutInvalidation"})	
	public void testConflictException() {
		/**
		 * Create:
		 * <pre>
		 *       o
		 *       | 1/ok
		 *       o
		 *       | 1/ok
		 *  -----o-----
		 *  | 4/ok    | 2/ok
		 *  *         *
		 * </pre>
		 */		
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok","ok","ok"), 6, true);
		reuseOracle.getReuseTree().insert(getInput(1, 1, 4), qr);
		
		qr = new QueryResult<Integer, String>(getOutput("ok","ok","ok"), 4, true);
		reuseOracle.getReuseTree().insert(getInput(1, 1, 2), qr);
		
		// Here reuse tree should throw an exception when adding (113/ok differentout notimportant)
		qr = new QueryResult<Integer, String>(getOutput("ok","different","notimp"), 5, true);
		reuseOracle.getReuseTree().insert(getInput(1, 1, 3), qr);
	}

	@Test(dependsOnMethods = {"testNoReusePossible"})
	public void testReuseNodePrefixWhileReusing() {
		reuseOracle.getReuseTree().useModelInvariantSymbols(true);
		reuseOracle.getReuseTree().addInvariantInputSymbol(0);
		
		QueryResult<Integer, String> qr = new QueryResult<Integer, String>(getOutput("ok","ok","ok"), 2, true);
		reuseOracle.getReuseTree().insert(getInput(1, 0, 1), qr);
		
		Word<Integer> input = getInput(1,0,1,1);
		NodeResult<Integer, Integer, String> node = reuseOracle.getReuseTree().getReuseableSystemState(input);

		Assert.assertTrue(node.prefixLength == 3); // ''1 0 1''
		// reuse the prefix
		qr = new QueryResult<Integer, String>(getOutput("ok"), 3, true);
		reuseOracle.getReuseTree().insert(getInput(1), node.s, qr);
		
		// The "1 1" system state should not be available:
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1));
		Assert.assertNull(node);
		
		// There should be a "1 1 1" system state, even this query was never seen
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,1,1));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 3); // query "1 1 1"
		
		// There should be a "1 0 0 0 0 1 1" system state, even this query was never seen
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,0,0,0,0,1,1));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 7);
		
		// There should be a system state for "1 0 0 0 0 1 1", even this query was never seen
		node = reuseOracle.getReuseTree().getReuseableSystemState(getInput(1,0,0,0,0,1,1,0,1));
		Assert.assertNotNull(node);
		Assert.assertTrue(node.prefixLength == 7); // so remaining 2 symbols
	}
	
	private Word<Integer> getInput(Integer... param){
		WordBuilder<Integer> wb = new WordBuilder<>();
		for (int j=0; j<param.length; j++) {
			wb.add(param[j]);
		}
		return wb.toWord();
	}
	
	private Word<String> getOutput(String... param){
		WordBuilder<String> wb = new WordBuilder<>();
		for (int j=0; j<param.length; j++) {
			wb.add(param[j]);
		}
		return wb.toWord();
	}
}