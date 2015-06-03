/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.eqtests.basic;

import net.automatalib.automata.concepts.InputAlphabetHolder;
import de.learnlib.api.EquivalenceOracle;
import de.learnlib.oracles.DefaultQuery;

public class SimpleEQOracle<A extends InputAlphabetHolder<I>,I,D> {
	
	public static <A extends InputAlphabetHolder<I>,I,D>
	SimpleEQOracle<A,I,D> create(EquivalenceOracle<A,I,D> eqOracle) {
		return new SimpleEQOracle<A,I,D>(eqOracle);
	}
	
	private final EquivalenceOracle<A, I, D> eqOracle;
	
	public SimpleEQOracle(EquivalenceOracle<A,I,D> eqOracle) {
		this.eqOracle = eqOracle;
	}
	
	public DefaultQuery<I,D> findCounterExample(A hypothesis) {
		return eqOracle.findCounterExample(hypothesis, hypothesis.getInputAlphabet());
	}
}
