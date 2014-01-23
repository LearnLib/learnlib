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

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.incremental.mealy.dag.IncrementalMealyDAGBuilder;
import net.automatalib.incremental.mealy.dag.State;
import net.automatalib.incremental.mealy.dag.TransitionRecord;
import net.automatalib.words.Alphabet;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.SUL;
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
public class SULCache<I, O> implements SUL<I, O> {
	
	private final IncrementalMealyDAGBuilder<I, O> incMealy;
	private final MealyMachine<State,I,TransitionRecord,O> mealyView;
	private final SUL<I,O> delegate;
	
	private State current;
	private final WordBuilder<I> inputWord = new WordBuilder<>();
	private WordBuilder<O> outputWord;

	public SULCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		this.incMealy = new IncrementalMealyDAGBuilder<>(alphabet);
		this.mealyView = incMealy.asAutomaton();
		this.delegate = sul;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.SUL#pre()
	 */
	@Override
	public void pre() {
		if(outputWord != null) {
			incMealy.insert(inputWord.toWord(), outputWord.toWord());
		}
		
		inputWord.clear();
		outputWord = null;
		current = mealyView.getInitialState();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.SUL#step(java.lang.Object)
	 */
	@Override
	public O step(I in) {
		O out = null;
		
		if(current != null) {
			TransitionRecord trans = mealyView.getTransition(current, in);
			
			if(trans != null) {
				out = mealyView.getTransitionOutput(trans);
				current = mealyView.getSuccessor(trans);
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
	
	public MealyCacheConsistencyTest<I, O> createCacheConsistencyTest() {
		return new MealyCacheConsistencyTest<>(incMealy);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.SUL#post()
	 */
    @Override
    public void post() {
        delegate.post();
    }


}
