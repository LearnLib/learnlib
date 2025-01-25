/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.algorithm.adt.config;

import java.util.Collections;
import java.util.Set;

import de.learnlib.algorithm.adt.adt.ADT;
import de.learnlib.algorithm.adt.api.SubtreeReplacer;
import de.learnlib.algorithm.adt.config.model.calculator.BestEffortCalculator;
import de.learnlib.algorithm.adt.config.model.calculator.MinLengthCalculator;
import de.learnlib.algorithm.adt.config.model.calculator.MinSizeCalculator;
import de.learnlib.algorithm.adt.config.model.replacer.ExhaustiveReplacer;
import de.learnlib.algorithm.adt.config.model.replacer.LevelOrderReplacer;
import de.learnlib.algorithm.adt.config.model.replacer.SingleReplacer;
import de.learnlib.algorithm.adt.model.ReplacementResult;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;

/**
 * A collection of default {@link SubtreeReplacer} configurations.
 */
public final class SubtreeReplacers {

    public static final SubtreeReplacer NEVER_REPLACE = new SubtreeReplacer() {

        @Override
        public <S, I, O> Set<ReplacementResult<S, I, O>> computeReplacements(MealyMachine<S, I, ?, O> hypothesis,
                                                                             Alphabet<I> inputs,
                                                                             ADT<S, I, O> adt) {
            return Collections.emptySet();
        }
    };

    public static final SubtreeReplacer LEVELED_BEST_EFFORT = new LevelOrderReplacer(new BestEffortCalculator());

    public static final SubtreeReplacer LEVELED_MIN_LENGTH = new LevelOrderReplacer(new MinLengthCalculator());
    public static final SubtreeReplacer LEVELED_MIN_SIZE = new LevelOrderReplacer(new MinSizeCalculator());
    public static final SubtreeReplacer EXHAUSTIVE_BEST_EFFORT = new ExhaustiveReplacer(new BestEffortCalculator());

    public static final SubtreeReplacer EXHAUSTIVE_MIN_LENGTH = new ExhaustiveReplacer(new MinLengthCalculator());
    public static final SubtreeReplacer EXHAUSTIVE_MIN_SIZE = new ExhaustiveReplacer(new MinSizeCalculator());
    public static final SubtreeReplacer SINGLE_BEST_EFFORT = new SingleReplacer(new BestEffortCalculator());

    public static final SubtreeReplacer SINGLE_MIN_LENGTH = new SingleReplacer(new MinLengthCalculator());
    public static final SubtreeReplacer SINGLE_MIN_SIZE = new SingleReplacer(new MinSizeCalculator());

    private SubtreeReplacers() {
        // prevent instantiation
    }
}
