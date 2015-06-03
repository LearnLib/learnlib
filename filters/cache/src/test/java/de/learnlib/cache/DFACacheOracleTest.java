/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @author Oliver Bauer 
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
