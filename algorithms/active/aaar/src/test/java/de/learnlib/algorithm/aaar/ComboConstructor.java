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
package de.learnlib.algorithm.aaar;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm;
import net.automatalib.alphabet.SupportsGrowingAlphabet;

/**
 * Utility interface to establish the combined learner constraints on {@link LearningAlgorithm} and
 * {@link SupportsGrowingAlphabet}.
 */
public interface ComboConstructor<L extends LearningAlgorithm<?, I, D> & SupportsGrowingAlphabet<I>, I, D>
        extends LearnerConstructor<L, I, D> {}
