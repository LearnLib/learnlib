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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.cache.sul;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.tree.IncrementalMealyTreeBuilder;
import net.automatalib.ts.transout.MealyTransitionSystem;
import net.automatalib.words.Alphabet;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.SUL;
import de.learnlib.cache.LearningCache.MealyLearningCache;
import de.learnlib.cache.mealy.MealyCacheConsistencyTest;

/**
 * A cache to be used with a {@link SUL}.
 * <p>
 * Because on a {@link SUL}, a query is executed step-by-step, it is impossible
 * to determine in advance whether the cached information is sufficient to answer
 * the complete query. However, in general it is undesired to execute any actions
 * on the underlying SUL as long as the requested information can be provided from
 * the cache.
 * <p>
 * This class therefore defers any real execution to the point where the cached information
 * is definitely insufficient; if such a point is not reached before a call to {@link #post()}
 * is made, the underlying SUL is not queried.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
@ParametersAreNonnullByDefault
public class SULCache<I, O> implements SUL<I, O>, MealyLearningCache<I,O> {
	
	public static <I,O>
	SULCache<I,O> createTreeCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return new SULCache<>(new IncrementalMealyTreeBuilder<I,O>(alphabet), sul);
	}
	
	public static <I,O>
	SULCache<I,O> createDAGCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		return new SULCache<>(new IncrementalMealyDAGBuilder<I,O>(alphabet), sul);
	}
	
	/**
	 * Implementation class; we need this to bind the {@code T} and {@code S}
	 * type parameters of the transition system returned by
	 * {@link IncrementalMealyBuilder#asTransitionSystem()}.
	 * 
	 * @author Malte Isberner <malte.isberner@gmail.com>
	 *
	 * @param <S> transition system state type
	 * @param <I> input symbol type
	 * @param <T> transition system transition type
	 * @param <O> output symbol type
	 */
	@ParametersAreNonnullByDefault
	private static final class SULCacheImpl<S,I,T,O> {
		private final IncrementalMealyBuilder<I, O> incMealy;
		private final MealyTransitionSystem<S,I,T,O> mealyTs;
		private final SUL<I,O> delegate;
		
		private S current;
		private final WordBuilder<I> inputWord = new WordBuilder<>();
		private WordBuilder<O> outputWord;
		
		public SULCacheImpl(IncrementalMealyBuilder<I,O> incMealy, MealyTransitionSystem<S,I,T,O> mealyTs, SUL<I,O> sul) {
			this.incMealy = incMealy;
			this.mealyTs = mealyTs;
			this.delegate = sul;
		}
		
		public void pre() {
			this.current = mealyTs.getInitialState();
		}
		
		@Nullable
		public O step(@Nullable I in) {
			O out = null;
			
			if(current != null) {
				T trans = mealyTs.getTransition(current, in);
				
				if(trans != null) {
					out = mealyTs.getTransitionOutput(trans);
					current = mealyTs.getSuccessor(trans);
					assert current != null;
				}
				else {
					current = null;
					outputWord = new WordBuilder<>();
					delegate.pre();
					for(I prevSym : inputWord) {
						outputWord.append(delegate.step(prevSym));
					}
				}
			}
			
			inputWord.append(in);
			
			if(current == null) {
				out = delegate.step(in);
				outputWord.add(out);
			}
			
			return out;
		}
		
		public void post() {
			if(outputWord != null) {
				incMealy.insert(inputWord.toWord(), outputWord.toWord());
			}
			
			inputWord.clear();
			outputWord = null;
			current = null;
		}
		
		@Nonnull
		public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
			return new MealyCacheConsistencyTest<>(incMealy);
		}
	}
	
	private final SULCacheImpl<?,I,?,O> impl;

	/**
	 * Constructor.
	 * @param alphabet the input alphabet
	 * @param sul the system under learning
	 * @deprecated since 2014-01-24. Use {@link de.learnlib.cache.Caches#createSULCache(Alphabet, SUL)}
	 */
	@Deprecated
	public SULCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		this(new IncrementalMealyDAGBuilder<I,O>(alphabet), sul);
	}
	public SULCache(IncrementalMealyBuilder<I, O> incMealy, SUL<I,O> sul) {
		this.impl = new SULCacheImpl<>(incMealy, incMealy.asTransitionSystem(), sul);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.SUL#pre()
	 */
	@Override
	public void pre() {
		impl.pre();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.SUL#step(java.lang.Object)
	 */
	@Override
	public O step(I in) {
		return impl.step(in);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.cache.LearningCache#createCacheConsistencyTest()
	 */
	@Override
	public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
		return impl.createCacheConsistencyTest();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.SUL#post()
	 */
    @Override
    public void post() {
        impl.post();
    }


}
