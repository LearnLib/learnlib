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
package de.learnlib.algorithms.adt.util;

import de.learnlib.api.SUL;
import de.learnlib.api.SULException;
import net.automatalib.automata.transout.impl.compact.CompactMealy;

/**
 * Utility class to wrap {@link CompactMealy}s in a {@link SUL} interface.
 *
 * @param <I> input (symbol) type
 * @param <O> output (symbol) type
 * @author frohme.
 */
public class CompactMealyWrapperSUL<I, O> implements SUL<I, O> {

	private CompactMealy<I, O> mealyMachine;

	private Integer state;

	public CompactMealyWrapperSUL(final CompactMealy<I, O> mealyMachine) {
		this.mealyMachine = mealyMachine;
	}

	@Override
	public void pre() {
		this.state = this.mealyMachine.getInitialState();
	}

	@Override
	public void post() {
	}

	@Override
	public O step(final I s) throws SULException {

		final O result = this.mealyMachine.getOutput(this.state, s);
		this.state = this.mealyMachine.getSuccessor(this.state, s);

		return result;
	}
}
