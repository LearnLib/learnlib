/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.api.algorithm.feature;

import de.learnlib.api.algorithm.LearningAlgorithm;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * Common interface for learning algorithms that use a global suffix set. These are mostly algorithms using an
 * <em>observation table</em>, such as Dana Angluin's L* and its derivatives.
 *
 * @param <M>
 *         hypothesis model type
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 *
 * @author Malte Isberner
 */
public interface GlobalSuffixLearner<M, I, D> extends LearningAlgorithm<M, I, D>, GlobalSuffixFeature<I> {

    interface GlobalSuffixLearnerDFA<I> extends GlobalSuffixLearner<DFA<?, I>, I, Boolean> {}

    interface GlobalSuffixLearnerMealy<I, O> extends GlobalSuffixLearner<MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
