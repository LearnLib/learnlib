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
package de.learnlib.algorithm.dhc.mealy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.common.util.HashUtil;
import net.automatalib.common.util.mapping.MutableMapping;
import net.automatalib.word.Word;

/**
 * Class that contains all data that represent the internal state of the {@link MealyDHC} learner.
 *
 * @param <I>
 *         The input alphabet type.
 * @param <O>
 *         The output alphabet type.
 */
public class MealyDHCState<I, O> {

    private final Set<Word<I>> splitters;
    private final CompactMealy<I, O> hypothesis;
    private final Map<Integer, MealyDHC.QueueElement<I, O>> accessSequences;

    MealyDHCState(Set<Word<I>> splitters,
                  CompactMealy<I, O> hypothesis,
                  MutableMapping<Integer, MealyDHC.QueueElement<I, O>> accessSequences) {
        this.splitters = splitters;
        this.hypothesis = hypothesis;
        this.accessSequences = new HashMap<>(HashUtil.capacity(hypothesis.size()));

        for (Integer s : hypothesis.getStates()) {
            final MealyDHC.QueueElement<I, O> elem = accessSequences.get(s);
            if (elem != null) {
                this.accessSequences.put(s, elem);
            }
        }
    }

    Set<Word<I>> getSplitters() {
        return splitters;
    }

    CompactMealy<I, O> getHypothesis() {
        return hypothesis;
    }

    Map<Integer, MealyDHC.QueueElement<I, O>> getAccessSequences() {
        return accessSequences;
    }
}
