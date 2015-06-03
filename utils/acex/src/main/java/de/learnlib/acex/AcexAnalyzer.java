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
package de.learnlib.acex;

/**
 * Interface for an analyzer of {@link AbstractCounterexample}s.
 * 
 * @author Malte Isberner
 *
 */
public interface AcexAnalyzer {
	
	/**
	 * Analyzes an abstract counterexample. This method returns the index of
	 * the corresponding distinguishing suffix.
	 * 
	 * @param acex the abstract counterexample
	 * @return the suffix index
	 */
	default public int analyzeAbstractCounterexample(AbstractCounterexample acex) {
		return analyzeAbstractCounterexample(acex, 0, acex.getLength());
	}
	
	default public int analyzeAbstractCounterexample(AbstractCounterexample acex, int low) {
		return analyzeAbstractCounterexample(acex, low, acex.getLength());
	}
	
	public int analyzeAbstractCounterexample(AbstractCounterexample acex, int low, int high);
	
}
