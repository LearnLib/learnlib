/* Copyright (C) 2013-2014 TU Dortmund
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
package de.learnlib.cache;

import java.util.ArrayList;
import java.util.Collection;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.cache.dfa.DFACacheOracle;
import de.learnlib.examples.dfa.ExampleAngluin;
import de.learnlib.oracles.CounterOracle;
import de.learnlib.oracles.DefaultQuery;
import de.learnlib.oracles.SimulatorOracle;

/**
 * A simple test against the {@link DFACacheOracle}.
 *  
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
@SuppressWarnings("deprecation")
public class DFACacheOracleTest {

	private MembershipOracle<Integer, Boolean> oracle;
	private CounterOracle<Integer, Boolean> counterOracle;
	private Collection<Query<Integer, Boolean>> queries;
	private long count;

	@BeforeClass
	public void setup() {
		// use angluin's example
		ExampleAngluin exampleAngluin = ExampleAngluin.createExample();
		DFA<?,Integer> fm = exampleAngluin.getReferenceAutomaton();
		Alphabet<Integer> alphabet = exampleAngluin.getAlphabet();

		// use simulated environment
		SimulatorOracle<Integer, Boolean> simulatorOracle = new SimulatorOracle<>(
				fm);

		// we count the number of delegated queries from the cache to this
		// counter to the simulator
		counterOracle = new CounterOracle<>(simulatorOracle, "counterOracle");

		// we query against the DFA cache, duplicate queries should be filtered
		oracle = new DFACacheOracle<>(alphabet, counterOracle);
		
		queries = new ArrayList<>();
	}

	@Test
	public void testNoQueriesReceived() {
		Assert.assertTrue(queries.size() == 0);
		oracle.processQueries(queries);
		Assert.assertTrue(counterOracle.getCount() == 0);
	}

	@Test(dependsOnMethods = { "testNoQueriesReceived" })
	public void testFirstQuery() {
		queries.add(new DefaultQuery<Integer, Boolean>(Word.fromLetter(0)));

		Assert.assertTrue(queries.size() == 1);
		oracle.processQueries(queries);
		Assert.assertTrue(counterOracle.getCount() == 1);
	}

	@Test(dependsOnMethods = { "testFirstQuery" })
	public void testFirstDuplicate() {
		Assert.assertTrue(queries.size() == 1);
		oracle.processQueries(queries);
		Assert.assertTrue(counterOracle.getCount() == 1);
	}

	@Test(dependsOnMethods = { "testFirstDuplicate" })
	public void testTwoQueriesOneDuplicate() {
		queries.add(new DefaultQuery<Integer, Boolean>(Word.fromSymbols(0,
				0)));
		Assert.assertTrue(queries.size() == 2);
		oracle.processQueries(queries);
		Assert.assertTrue(counterOracle.getCount() == 2);
	}

	@Test(dependsOnMethods = { "testTwoQueriesOneDuplicate" })
	public void testOneNewQuery() {
		queries.clear();

		Assert.assertTrue(queries.size() == 0);
		queries.add(new DefaultQuery<Integer, Boolean>(Word.fromLetter(1)));
		Assert.assertTrue(queries.size() == 1);
		oracle.processQueries(queries);
		count = counterOracle.getCount();
		Assert.assertTrue(count == 3);
	}
}
