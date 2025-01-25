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

import de.learnlib.algorithm.adt.adt.ADTNode;
import de.learnlib.algorithm.adt.api.ADTExtender;
import de.learnlib.algorithm.adt.api.PartialTransitionAnalyzer;
import de.learnlib.algorithm.adt.automaton.ADTHypothesis;
import de.learnlib.algorithm.adt.automaton.ADTState;
import de.learnlib.algorithm.adt.config.model.calculator.BestEffortDefensiveCalculator;
import de.learnlib.algorithm.adt.config.model.extender.DefaultExtender;
import de.learnlib.algorithm.adt.model.ExtensionResult;

/**
 * A collection of default {@link ADTExtender} configurations.
 */
public final class ADTExtenders {

    public static final ADTExtender NOP = new ADTExtender() {

        @Override
        public <I, O> ExtensionResult<ADTState<I, O>, I, O> computeExtension(ADTHypothesis<I, O> hypothesis,
                                                                             PartialTransitionAnalyzer<ADTState<I, O>, I> partialTransitionAnalyzer,
                                                                             ADTNode<ADTState<I, O>, I, O> ads) {
            return ExtensionResult.empty();
        }
    };

    public static final ADTExtender EXTEND_BEST_EFFORT = new DefaultExtender(new BestEffortDefensiveCalculator());

    private ADTExtenders() {
        // prevent instantiation
    }
}
