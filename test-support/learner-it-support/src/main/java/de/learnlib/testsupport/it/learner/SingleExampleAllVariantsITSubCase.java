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
