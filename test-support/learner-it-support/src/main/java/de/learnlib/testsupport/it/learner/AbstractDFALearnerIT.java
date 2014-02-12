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

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;

import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.examples.LearningExample.DFALearningExample;
import de.learnlib.examples.LearningExamples;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.DFALearnerVariantListImpl;


/**
 * Abstract integration test for DFA learning algorithms.
 * <p>
 * If run, this integration test tests the functionality of all
 * {@link #addLearnerVariants(Alphabet, DFAMembershipOracle, DFALearnerVariantList) variants}
 * of a DFA learning algorithm against all the examples contained in
 * {@link LearningExamples#createDFAExamples()}.
 * 
 * @author Malte Isberner
 *
 */
public abstract class AbstractDFALearnerIT extends AbstractLearnerIT {

	

	@Override
	public SingleExampleAllVariantsITSubCase<?,?,?>[] createExampleITCases() {
		List<? extends DFALearningExample<?>> examples = LearningExamples.createDFAExamples();
		
		SingleExampleAllVariantsITSubCase<?,?,?>[] result = new SingleExampleAllVariantsITSubCase[examples.size()];
		int i = 0;
		for(DFALearningExample<?> example : examples) {
			result[i++] = createAllVariantsITCase(example);
		}
		
		return result;
	}
	
	private <I>
	SingleExampleAllVariantsITSubCase<I, Boolean, DFA<?,I>> createAllVariantsITCase(DFALearningExample<I> example) {
		Alphabet<I> alphabet = example.getAlphabet();
		DFAMembershipOracle<I> mqOracle
			= new DFASimulatorOracle<>(example.getReferenceAutomaton());
		DFALearnerVariantListImpl<I> variants = new DFALearnerVariantListImpl<>();
		addLearnerVariants(alphabet, mqOracle, variants);
		
		return new SingleExampleAllVariantsITSubCase<>(example, variants);
	}
	
	/**
	 * Adds, for a given setup, all the variants of the DFA learner to be tested
	 * to the specified {@link LearnerVariantList variant list}.
	 * 
	 * @param alphabet the input alphabet
	 * @param mqOracle the membership oracle
	 * @param variants list to add the learner variants to
	 */
	protected abstract <I> void addLearnerVariants(
			Alphabet<I> alphabet,
			DFAMembershipOracle<I> mqOracle,
			DFALearnerVariantList<I> variants);
}
