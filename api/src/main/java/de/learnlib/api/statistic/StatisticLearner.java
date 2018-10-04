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
package de.learnlib.api.statistic;

import javax.annotation.Nonnull;

import de.learnlib.api.algorithm.LearningAlgorithm;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;

/**
 * Common interface for learners keeping statistics.
 *
 * @param <M>
 *         the automaton type
 * @param <I>
 *         input symbol class
 * @param <D>
 *         output symbol class
 *
 * @author Jeroen Meijer
 */
public interface StatisticLearner<M, I, D> extends LearningAlgorithm<M, I, D> {

    @Nonnull
    StatisticData getStatisticalData();

    interface DFAStatisticLearner<I> extends StatisticLearner<DFA<?, I>, I, Boolean> {}

    interface MealyStatisticLearner<I, O> extends StatisticLearner<MealyMachine<?, I, ?, O>, I, Word<O>> {}
}
