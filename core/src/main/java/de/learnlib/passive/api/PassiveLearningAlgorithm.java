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
