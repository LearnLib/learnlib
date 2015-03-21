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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.oracles.DefaultQuery;

public interface PassiveLearningAlgorithm<M, I, D> {
	
	public void addSamples(Collection<? extends DefaultQuery<I,D>> samples);
	
	@SuppressWarnings("unchecked")
	default public void addSamples(DefaultQuery<I,D> ...samples) {
		addSamples(Arrays.asList(samples));
	}
	
	default public void addSample(DefaultQuery<I,D> sample) {
		addSamples(Collections.singleton(sample));
	}
	
	default public void addSample(Word<I> input, D output) {
		addSample(new DefaultQuery<>(input, output));
	}
	
	default public void addSamples(D output, Collection<? extends Word<I>> words) {
		List<DefaultQuery<I,D>> queries = new ArrayList<>(words.size());
		for (Word<I> word : words) {
			queries.add(new DefaultQuery<>(word, output));
		}
	}
	
	@SuppressWarnings("unchecked")
	default public void addSamples(D output, Word<I> ...words) {
		addSamples(output, Arrays.asList(words));
	}
	
	
	public M computeModel();

}
