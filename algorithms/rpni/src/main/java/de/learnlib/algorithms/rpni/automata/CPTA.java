/* Copyright (C) 2017 TU Dortmund
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
package de.learnlib.algorithms.rpni.automata;

import net.automatalib.words.Alphabet;

import java.util.Arrays;

/**
 * Extension to the {@link PTA} that additionally stores coverage information (how often a state has been visited) for
 * each state.
 *
 * @param <I> Type of input symbols
 *
 * @author frohme
 */
public class CPTA<I> extends PTA<I> {

	private int[] coverage;

	public CPTA(final Alphabet<I> inputAlphabet) {
		super(inputAlphabet);
		this.coverage = new int[super.stateCapacity];
	}

	public CPTA(final CPTA<I> that) {
		super(that);
		this.coverage = Arrays.copyOf(that.coverage, that.coverage.length);
	}

	public void setCoverage(final int state, final int cardinality) {
		this.coverage[state] = cardinality;
	}

	public int getCoverage(final int state) {
		return this.coverage[state];
	}

	@Override
	public void ensureCapacity(final int newCapacity) {

		final int oldCap = super.stateCapacity;
		super.ensureCapacity(newCapacity);
		final int newCap = super.stateCapacity;

		if (newCap > oldCap) {
			final int[] newCoverage = new int[newCap];

			System.arraycopy(coverage, 0, newCoverage, 0, coverage.length);

			Arrays.fill(newCoverage, oldCap, newCoverage.length, INVALID_STATE);

			this.coverage = newCoverage;
		}
	}
}
