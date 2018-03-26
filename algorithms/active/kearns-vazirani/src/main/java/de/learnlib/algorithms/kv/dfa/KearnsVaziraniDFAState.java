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
package de.learnlib.algorithms.kv.dfa;

import java.io.Serializable;
import java.util.List;

import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.datastructure.discriminationtree.BinaryDTree;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

/**
 * Class that contains all data that represent the internal state of the {@link KearnsVaziraniDFA} learner.
 *
 * @param <I>
 *         The input alphabet type.
 *
 * @author bainczyk
 */
public class KearnsVaziraniDFAState<I> implements Serializable {

    private final CompactDFA<I> hypothesis;
    private final BinaryDTree<I, StateInfo<I, Boolean>> discriminationTree;
    private final List<StateInfo<I, Boolean>> stateInfos;

    KearnsVaziraniDFAState(final CompactDFA<I> hypothesis,
                           final BinaryDTree<I, StateInfo<I, Boolean>> discriminationTree,
                           final List<StateInfo<I, Boolean>> stateInfos) {
        this.hypothesis = hypothesis;
        this.discriminationTree = discriminationTree;
        this.stateInfos = stateInfos;
    }

    CompactDFA<I> getHypothesis() {
        return hypothesis;
    }

    BinaryDTree<I, StateInfo<I, Boolean>> getDiscriminationTree() {
        return discriminationTree;
    }

    List<StateInfo<I, Boolean>> getStateInfos() {
        return stateInfos;
    }
}
