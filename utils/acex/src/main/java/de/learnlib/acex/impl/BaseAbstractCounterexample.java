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
package de.learnlib.acex.impl;

import java.util.Arrays;

import de.learnlib.acex.AbstractCounterexample;

/**
 * Base class for abstract counterexamples.
 * 
 * @author Malte Isberner
 *
 */
public abstract class BaseAbstractCounterexample implements AbstractCounterexample {
	
	private final int[] values;
	
	/**
	 * Constructor.
	 * @param m length of the counterexample
	 */
	public BaseAbstractCounterexample(int m) {
		this.values = new int[m + 1];
		Arrays.fill(values, -1);
		values[0] = 0;
		values[m] = 1;
	}
	
	/**
	 * Retrieves the length of the abstract counterexample
	 * @return the length of the counterexample
	 */
	@Override
	public int getLength() {
		return values.length - 1;
	}
	
	/**
	 * Tests the effect of performing a prefix transformation for
	 * the given index. If the prefix transformation causes hypothesis
	 * and target system to agree, 1 is returned, and 0 otherwise.
	 * <p>
	 * This method corresponds to the &alpha; mapping from the paper.
	 * 
	 * @param index the index for the prefix transformation
	 * @return 1 if prefix transformation causes target and hypothesis
	 * to agree, 0 otherwise.
	 */
	public int test(int index) {
		if(index < 0 || index >= values.length) {
			throw new IndexOutOfBoundsException("" + index);
		}
		
		if(values[index] == -1) {
			values[index] = computeEffect(index);
		}
		return values[index];
	}
	
	/**
	 * Computes the effect of a prefix transformation.
	 */
	protected abstract int computeEffect(int index);
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(values.length);
		
		for (int v : values) {
			if (v == 0) {
				sb.append('0');
			}
			else if (v == 1) {
				sb.append('1');
			}
			else { // v == -1
				sb.append('?');
			}
		}
		
		return sb.toString();
	}
}
