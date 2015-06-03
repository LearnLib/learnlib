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
