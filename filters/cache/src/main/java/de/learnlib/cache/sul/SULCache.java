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

import net.automatalib.incremental.mealy.IncrementalMealyBuilder;
import net.automatalib.incremental.mealy.State;
import net.automatalib.incremental.mealy.TransitionRecord;
import net.automatalib.words.Alphabet;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.SUL;
import de.learnlib.cache.mealy.MealyCacheConsistencyTest;

public class SULCache<I, O> implements SUL<I, O> {
	
	private final IncrementalMealyBuilder<I, O> incMealy;
	private final SUL<I,O> delegate;
	
	private State current;
	private final WordBuilder<I> inputWord = new WordBuilder<>();
	private WordBuilder<O> outputWord;

	public SULCache(Alphabet<I> alphabet, SUL<I,O> sul) {
		this.incMealy = new IncrementalMealyBuilder<>(alphabet);
		this.delegate = sul;
	}

	@Override
	public void pre() {
		if(outputWord != null) {
			incMealy.insert(inputWord.toWord(), outputWord.toWord());
		}
		
		inputWord.clear();
		outputWord = null;
		current = incMealy.getInitialState();
	}

	@Override
	public O step(I in) {
		O out = null;
		
		if(current != null) {
			TransitionRecord trans = incMealy.getTransition(current, in);
			
			if(trans != null) {
				out = incMealy.getTransitionOutput(trans);
				current = incMealy.getSuccessor(trans);
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

    @Override
    public void post() {
        delegate.post();
    }


}
