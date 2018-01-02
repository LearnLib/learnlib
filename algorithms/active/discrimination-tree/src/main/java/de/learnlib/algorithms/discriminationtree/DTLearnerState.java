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
package de.learnlib.algorithms.discriminationtree;

import java.io.Serializable;

import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDiscriminationTree;

/**
 * Class that contains all data that represent the internal state of the {@link DTLearnerState} learner and its DFA and
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
 *
 * @author bainczyk
 */
public class DTLearnerState<I, D, SP, TP> implements Serializable {

    private final AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> dtree;
    private final DTLearnerHypothesis<I, D, SP, TP> hypothesis;

    DTLearnerState(final AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> dtree,
                   final DTLearnerHypothesis<I, D, SP, TP> hypothesis) {
        this.dtree = dtree;
        this.hypothesis = hypothesis;
    }

    AbstractWordBasedDiscriminationTree<I, D, HState<I, D, SP, TP>> getDtree() {
        return dtree;
    }

    DTLearnerHypothesis<I, D, SP, TP> getHypothesis() {
        return hypothesis;
    }
}
