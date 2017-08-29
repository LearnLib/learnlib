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
package de.learnlib.algorithms.adt.config;

import de.learnlib.algorithms.adt.adt.ADT;
import de.learnlib.algorithms.adt.api.SubtreeReplacer;
import de.learnlib.algorithms.adt.config.model.calculator.BestEffortCalculator;
import de.learnlib.algorithms.adt.config.model.calculator.MinLengthCalculator;
import de.learnlib.algorithms.adt.config.model.calculator.MinSizeCalculator;
import de.learnlib.algorithms.adt.config.model.replacer.ExhaustiveReplacer;
import de.learnlib.algorithms.adt.config.model.replacer.LevelOrderReplacer;
import de.learnlib.algorithms.adt.config.model.replacer.SingleReplacer;
import de.learnlib.algorithms.adt.model.ReplacementResult;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;

import java.util.Collections;
import java.util.Set;

/**
 * A collection of default {@link SubtreeReplacer} configurations
 *
 * @author frohme
 */
public class SubtreeReplacers {

	public final static SubtreeReplacer NEVER_REPLACE = new SubtreeReplacer() {

		@Override
		public <S, I, O> Set<ReplacementResult<S, I, O>> computeReplacements(final MealyMachine<S, I, ?, O> hypothesis,
																			 final Alphabet<I> inputs,
																			 final ADT<S, I, O> adt) {
			return Collections.emptySet();
		}
	};

	public final static SubtreeReplacer LEVELED_BEST_EFFORT = new LevelOrderReplacer(new BestEffortCalculator());
	public final static SubtreeReplacer LEVELED_MIN_LENGTH = new LevelOrderReplacer(new MinLengthCalculator());
	public final static SubtreeReplacer LEVELED_MIN_SIZE = new LevelOrderReplacer(new MinSizeCalculator());

	public final static SubtreeReplacer EXHAUSTIVE_BEST_EFFORT = new ExhaustiveReplacer(new BestEffortCalculator());
	public final static SubtreeReplacer EXHAUSTIVE_MIN_LENGTH = new ExhaustiveReplacer(new MinLengthCalculator());
	public final static SubtreeReplacer EXHAUSTIVE_MIN_SIZE = new ExhaustiveReplacer(new MinSizeCalculator());

	public final static SubtreeReplacer SINGLE_BEST_EFFORT = new SingleReplacer(new BestEffortCalculator());
	public final static SubtreeReplacer SINGLE_MIN_LENGTH = new SingleReplacer(new MinLengthCalculator());
	public final static SubtreeReplacer SINGLE_MIN_SIZE = new SingleReplacer(new MinSizeCalculator());

}
