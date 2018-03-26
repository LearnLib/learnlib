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
package de.learnlib.algorithms.kv.mealy;

import java.io.Serializable;
import java.util.List;

import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;
import net.automatalib.automata.transout.impl.compact.CompactMealy;
import net.automatalib.words.Word;

/**
 * Class that contains all data that represent the internal state of the {@link KearnsVaziraniMealy} learner.
 *
 * @param <I>
 *         The input alphabet type.
 * @param <O>
 *         The output alphabet type.
 *
 * @author bainczyk
 */
public class KearnsVaziraniMealyState<I, O> implements Serializable {

    private final CompactMealy<I, O> hypothesis;
    private final AbstractWordBasedDiscriminationTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
    private final List<StateInfo<I, Word<O>>> stateInfos;

    KearnsVaziraniMealyState(final CompactMealy<I, O> hypothesis,
                             final AbstractWordBasedDiscriminationTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree,
                             final List<StateInfo<I, Word<O>>> stateInfos) {
        this.hypothesis = hypothesis;
        this.discriminationTree = discriminationTree;
        this.stateInfos = stateInfos;
    }

    CompactMealy<I, O> getHypothesis() {
        return hypothesis;
    }

    AbstractWordBasedDiscriminationTree<I, Word<O>, StateInfo<I, Word<O>>> getDiscriminationTree() {
        return discriminationTree;
    }

    List<StateInfo<I, Word<O>>> getStateInfos() {
        return stateInfos;
    }
}
