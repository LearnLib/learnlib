/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.cache.dfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.cache.LearningCacheOracle.DFALearningCacheOracle;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Word;

public class DFAHashCacheOracle<I> implements DFALearningCacheOracle<I> {
	
	
	private final MembershipOracle<I, Boolean> delegate;
	private final Map<Word<I>,Boolean> cache;
	private final Lock cacheLock;
	

	public DFAHashCacheOracle(MembershipOracle<I, Boolean> delegate) {
		this.delegate = delegate;
		this.cache = new HashMap<>();
		this.cacheLock = new ReentrantLock();
	}

	@Override
	public EquivalenceOracle<DFA<?, I>, I, Boolean> createCacheConsistencyTest() {
		return new DFAHashCacheConsistencyTest<>(cache, cacheLock);
	}

	@Override
	public void processQueries(Collection<? extends Query<I, Boolean>> queries) {
		List<ProxyQuery<I>> misses = new ArrayList<>();
		
		cacheLock.lock();
		try {
			for(Query<I,Boolean> qry : queries) {
				Word<I> input = qry.getInput();
				Boolean answer = cache.get(input);
				if(answer != null) {
					qry.answer(answer);
				}
				else {
					misses.add(new ProxyQuery<>(qry));
				}
			}
		}
		finally {
			cacheLock.unlock();
		}
		
		delegate.processQueries(misses);
		
		cacheLock.lock();
		try {
			for(ProxyQuery<I> miss : misses) {
				cache.put(miss.getInput(), miss.getAnswer());
			}
		}
		finally {
			cacheLock.unlock();
		}
	}

}
