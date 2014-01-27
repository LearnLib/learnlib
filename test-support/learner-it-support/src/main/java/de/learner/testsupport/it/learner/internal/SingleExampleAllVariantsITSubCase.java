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
package de.learner.testsupport.it.learner.internal;

import java.util.List;

import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;

import org.testng.annotations.Factory;

import de.learner.testsupport.it.learner.internal.LearnerVariant;
import de.learner.testsupport.it.learner.internal.LearnerVariantListImpl;
import de.learner.testsupport.it.learner.internal.SingleLearnerVariantITSubCase;
import de.learnlib.examples.LearningExample;

public final class SingleExampleAllVariantsITSubCase<I,O,
	A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, O>> {
	
	private final LearningExample<I, O, ? extends A> example;
	private final LearnerVariantListImpl<? extends A, I, O> variants;

	public SingleExampleAllVariantsITSubCase(LearningExample<I,O,? extends A> example,
			LearnerVariantListImpl<? extends A,I,O> variants) {
		this.example = example;
		this.variants = variants;
	}
	
	
	@Factory
	public SingleLearnerVariantITSubCase<?, ?, ?>[] createSingleVariantITCases() {
		List<? extends LearnerVariant<? extends A, I, O>> variantList = variants.getLearnerVariants();
		SingleLearnerVariantITSubCase<?, ?, ?>[] result = new SingleLearnerVariantITSubCase[variantList.size()];
		int i = 0;
		for(LearnerVariant<? extends A,I,O> variant : variantList) {
			result[i++] = new SingleLearnerVariantITSubCase<>(variant, example);
		}
		return result;
	}




}
