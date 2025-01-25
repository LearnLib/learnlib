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
package de.learnlib.algorithm.observationpack;

import de.learnlib.algorithm.observationpack.hypothesis.HState;
import de.learnlib.algorithm.observationpack.hypothesis.OPLearnerHypothesis;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;

/**
 * Class that contains all data that represent the internal state of the {@link OPLearnerState} learner and its DFA and
 * Mealy implementations.
 *
 * @param <I>
 *         The input alphabet type.
 * @param <D>
 *         The output domain type.
 * @param <SP>
 *         The state property type.
 * @param <TP>
 *         The transition property type.
 */
public class OPLearnerState<I, D, SP, TP> {

    private final AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> dtree;
    private final OPLearnerHypothesis<I, D, SP, TP> hypothesis;

    OPLearnerState(AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> dtree,
                   OPLearnerHypothesis<I, D, SP, TP> hypothesis) {
        this.dtree = dtree;
        this.hypothesis = hypothesis;
    }

    AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> getDtree() {
        return dtree;
    }

    OPLearnerHypothesis<I, D, SP, TP> getHypothesis() {
        return hypothesis;
    }
}
