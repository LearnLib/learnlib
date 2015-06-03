/* Copyright (C) 2015 TU Dortmund
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
package de.learnlib.passive.api;

import java.util.Collection;

import net.automatalib.automata.fsa.FiniteStateAcceptor;
import net.automatalib.words.Word;

public interface PassiveAcceptorLearner<M extends FiniteStateAcceptor<?, I>,I> extends PassiveLearningAlgorithm<M, I, Boolean> {
	
	default public void addPositiveSample(Word<I> word) {
		addSample(word, true);
	}
	
	default public void addPositiveSamples(Collection<? extends Word<I>> words) {
		addSamples(true, words);
	}
	
	@SuppressWarnings("unchecked")
	default public void addPositiveSamples(Word<I> ...words) {
		addSamples(true, words);
	}
	
	
	default public void addNegativeSample(Word<I> word) {
		addSample(word, false);
	}
	
	default public void addNegativeSamples(Collection<? extends Word<I>> words) {
		addSamples(false, words);
	}
	
	@SuppressWarnings("unchecked")
	default public void addNegativeSamples(Word<I> ...words) {
		addSamples(false, words);
	}
}
