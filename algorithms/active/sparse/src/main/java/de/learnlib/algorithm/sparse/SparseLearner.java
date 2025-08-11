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
package de.learnlib.algorithm.sparse;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;

import java.util.*;

/**
 * optimized implementation of the Ls learning algorithm,
 * as described in section 6 of the paper "Learning Mealy Machines with Sparse Observation Tables"
 */
public class SparseLearner<I, O> extends AbstractSparseLearner<Integer, I, O> {

    public SparseLearner(Alphabet<I> alphabet, MealyMembershipOracle<I, O> oracle) {
        this(alphabet, oracle, Collections.emptyList());
    }

    public SparseLearner(Alphabet<I> alphabet, MealyMembershipOracle<I, O> oracle, List<Word<I>> initialSuffixes) {
        super(alphabet, oracle, initialSuffixes, new CompactMealy<>(alphabet));
    }
}