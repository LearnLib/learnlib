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
package de.learnlib.algorithm.kv.mealy;

import java.util.List;

import de.learnlib.algorithm.kv.StateInfo;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;

/**
 * Class that contains all data that represent the internal state of the {@link KearnsVaziraniMealy} learner.
 *
 * @param <I>
 *         The input alphabet type.
 * @param <O>
 *         The output alphabet type.
 */
public class KearnsVaziraniMealyState<I, O> {

    private final CompactMealy<I, O> hypothesis;
    private final MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
    private final List<StateInfo<I, Word<O>>> stateInfos;

    KearnsVaziraniMealyState(CompactMealy<I, O> hypothesis,
                             MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree,
                             List<StateInfo<I, Word<O>>> stateInfos) {
        this.hypothesis = hypothesis;
        this.discriminationTree = discriminationTree;
        this.stateInfos = stateInfos;
    }

    CompactMealy<I, O> getHypothesis() {
        return hypothesis;
    }

    MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> getDiscriminationTree() {
        return discriminationTree;
    }

    List<StateInfo<I, Word<O>>> getStateInfos() {
        return stateInfos;
    }
}
