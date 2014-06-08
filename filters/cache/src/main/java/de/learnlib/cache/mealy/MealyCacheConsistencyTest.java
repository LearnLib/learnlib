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
package de.learnlib.cache.mealy;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * An {@link EquivalenceOracle} that tests an hypothesis for consistency with the
 * contents of a {@link MealyCacheOracle}.
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class MealyCacheConsistencyTest<I, O> implements
		MealyEquivalenceOracle<I,O> {
	
	private final IncrementalMealyBuilder<I, O> incMealy;
	private final Lock incMealyLock;
	
	/**
	 * Constructor.
	 * @param incMealy the {@link IncrementalMealyBuilder} data structure underlying the
	 * cache.
	 */
	public MealyCacheConsistencyTest(IncrementalMealyBuilder<I, O> incMealy, Lock lock) {
		this.incMealy = incMealy;
		this.incMealyLock = lock;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.EquivalenceOracle#findCounterExample(java.lang.Object, java.util.Collection)
	 */
	@Override
	public DefaultQuery<I, Word<O>> findCounterExample(
			MealyMachine<?, I, ?, O> hypothesis, Collection<? extends I> inputs) {
		WordBuilder<O> wb;
		Word<I> w;
		
		incMealyLock.lock();
		try {
			w = incMealy.findSeparatingWord(hypothesis, inputs, false);
			if(w == null)
				return null;
			wb = new WordBuilder<O>(w.length());
			incMealy.lookup(w, wb);
		}
		finally {
			incMealyLock.unlock();
		}
		
		DefaultQuery<I,Word<O>> result = new DefaultQuery<>(w);
		result.answer(wb.toWord());
		return result;
	}

}
