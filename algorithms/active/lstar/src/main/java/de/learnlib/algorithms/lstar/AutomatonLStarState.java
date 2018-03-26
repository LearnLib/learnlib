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
package de.learnlib.algorithms.lstar;

import java.util.List;

import de.learnlib.algorithms.lstar.AbstractAutomatonLStar.StateInfo;
import de.learnlib.datastructure.observationtable.GenericObservationTable;

/**
 * Class that contains all data that represent the internal state of the {@link AbstractAutomatonLStar} learner and its
 * DFA and Mealy implementations.
 *
 * @param <I>
 *         The input alphabet type.
 * @param <D>
 *         The output domain type.
 * @param <AI>
 *         The hypothesis type.
 * @param <S>
 *         The hypothesis state type.
 *
 * @author bainczyk
 */
public class AutomatonLStarState<I, D, AI, S> extends AbstractLStarState<I, D> {

    private final AI hypothesis;
    private final List<StateInfo<S, I>> stateInfos;

    AutomatonLStarState(final GenericObservationTable<I, D> observationTable,
                        final AI hypothesis,
                        final List<StateInfo<S, I>> stateInfos) {
        super(observationTable);
        this.hypothesis = hypothesis;
        this.stateInfos = stateInfos;
    }

    AI getHypothesis() {
        return hypothesis;
    }

    List<AbstractAutomatonLStar.StateInfo<S, I>> getStateInfos() {
        return stateInfos;
    }
}
