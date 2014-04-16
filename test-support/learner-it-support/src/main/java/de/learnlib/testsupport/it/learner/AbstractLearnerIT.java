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

import org.testng.annotations.Test;

/**
 * Abstract integration test for a learning algorithm (or "learner").
 * <p>
 * A learner integration test tests the functionality of a learning algorithm against
 * a well-defined set of example setups.
 * <p>
 * This class most probably does not need to be subclassed directly. Instead, extend
 * one of the existing subclasses.
 * 
 * @author Malte Isberner
 *
 */
public abstract class AbstractLearnerIT {

	/*
	 * FIXME: Nested @Factory not working as expected, so we use this workaround ...
	 */
	@Test
	public void testAll() {
		int testCounter = 0;
		for(SingleExampleAllVariantsITSubCase<?, ?, ?> exampleSubCase : createExampleITCases()) {
			for(SingleLearnerVariantITSubCase<?, ?, ?> variantSubCase : exampleSubCase.createSingleVariantITCases()) {
				variantSubCase.testLearning();
				testCounter++;
			}
		}
		System.out.println("Ran " + testCounter + " tests");
	}
	
	/**
	 * Creates an array of per-example test cases for all learner variants.
	 * @return the array of test cases, one for each example 
	 */
	public abstract SingleExampleAllVariantsITSubCase<?,?,?>[] createExampleITCases();

}
