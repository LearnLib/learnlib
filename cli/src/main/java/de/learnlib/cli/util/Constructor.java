/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.cli.util;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.cli.adapter.ProceduralDFAAdapter;
import de.learnlib.cli.adapter.ProceduralMealyAdapter;
import de.learnlib.oracle.AdaptiveMembershipOracle;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;

@FunctionalInterface
public interface Constructor<A extends Alphabet<I>, M, I, D, OR> {

    LearningAlgorithm<M, I, D> constructLearner(A alphabet, OR oracle);

    @FunctionalInterface
    interface PresetConstructor<A extends Alphabet<I>, M, I, D>
            extends Constructor<A, M, I, D, MembershipOracle<I, D>> {

    }

    @FunctionalInterface
    interface AdaptiveConstructor<A extends Alphabet<I>, M, I, O>
            extends Constructor<A, M, I, Word<O>, AdaptiveMembershipOracle<I, O>> {

    }

    @FunctionalInterface
    interface MealyConstructor<A extends Alphabet<I>, I, O>
            extends PresetConstructor<A, MealyMachine<?, I, ?, O>, I, Word<O>> {

        @Override
        ProceduralMealyAdapter<I, O> constructLearner(A alphabet, MembershipOracle<I, Word<O>> oracle);
    }

    @FunctionalInterface
    interface DFAConstructor<A extends Alphabet<I>, I> extends PresetConstructor<A, DFA<?, I>, I, Boolean> {

        @Override
        ProceduralDFAAdapter<I> constructLearner(A alphabet, MembershipOracle<I, Boolean> oracle);
    }
}
