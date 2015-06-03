/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.testsupport.it.learner;

import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;

import org.testng.annotations.Factory;

import de.learnlib.examples.LearningExample;
import de.learnlib.testsupport.it.learner.LearnerVariant;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl;
import de.learnlib.testsupport.it.learner.SingleLearnerVariantITSubCase;

final class SingleExampleAllVariantsITSubCase<I,D,
	A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>> {
	
	private final LearningExample<I, D, ? extends A> example;
	private final LearnerVariantListImpl<? extends A, I, D> variants;

	public SingleExampleAllVariantsITSubCase(LearningExample<I,D,? extends A> example,
			LearnerVariantListImpl<? extends A,I,D> variants) {
		this.example = example;
		this.variants = variants;
	}
	
	
	@Factory
	public SingleLearnerVariantITSubCase<?, ?, ?>[] createSingleVariantITCases() {
		List<? extends LearnerVariant<? extends A, I, D>> variantList = variants.getLearnerVariants();
		SingleLearnerVariantITSubCase<?, ?, ?>[] result = new SingleLearnerVariantITSubCase[variantList.size()];
		int i = 0;
		for(LearnerVariant<? extends A,I,D> variant : variantList) {
			result[i++] = new SingleLearnerVariantITSubCase<I,D,A>(variant, example);
		}
		return result;
	}




}
