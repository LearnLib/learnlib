/* Copyright (C) 2015 TU Dortmund
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
