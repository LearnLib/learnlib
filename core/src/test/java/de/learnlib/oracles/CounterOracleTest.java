/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.oracles;


import java.util.Collection;
import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import de.learnlib.api.Query;
import de.learnlib.testsupport.oracles.NoopOracle;
import de.learnlib.testsupport.queries.TestQueries;

@Test
public class CounterOracleTest {

	private static final String COUNTER_NAME = "testCounter";
	
	private final CounterOracle<Object, Object> oracle;
	
	public CounterOracleTest() {
		this.oracle = new CounterOracle<>(new NoopOracle<>(), COUNTER_NAME);
	}

	@Test
	public void testInitialState() {
		Assert.assertEquals(oracle.getCount(), 0L);
	}
	
	@Test(dependsOnMethods = "testInitialState")
	public void testFirstQueryBatch() {
		Collection<? extends Query<Object,Object>> queries = TestQueries.createNoopQueries(2);
		long oldCount = oracle.getCount();
		oracle.processQueries(queries);
		Assert.assertEquals(oracle.getCount(), oldCount + 2L);
	}
	
	@Test(dependsOnMethods = "testFirstQueryBatch")
	public void testEmptyQueryBatch() {
		Collection<? extends Query<Object,Object>> noQueries = Collections.emptySet();
		long oldCount = oracle.getCount();
		oracle.processQueries(noQueries);
		Assert.assertEquals(oracle.getCount(), oldCount);
	}
	
	@Test(dependsOnMethods = "testEmptyQueryBatch")
	public void testSecondQueryBatch() {
		Collection<? extends Query<Object,Object>> queries = TestQueries.createNoopQueries(1);
		long oldCount = oracle.getCount();
		oracle.processQueries(queries);
		Assert.assertEquals(oracle.getCount(), oldCount + 1L);
	}
	
	@Test
	public void testGetName() {
		Assert.assertEquals(oracle.getCounter().getName(), COUNTER_NAME);
	}
	
	@AfterMethod
	public void testInvariants() {
		Assert.assertEquals(oracle.getCounter().getCount(), oracle.getCount());
	}
	
	
}
