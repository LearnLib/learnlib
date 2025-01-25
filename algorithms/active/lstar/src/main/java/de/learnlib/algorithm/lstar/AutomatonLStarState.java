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
package de.learnlib.algorithm.lstar;

import de.learnlib.algorithm.lstar.AbstractAutomatonLStar.StateInfo;
import de.learnlib.datastructure.observationtable.GenericObservationTable;
import net.automatalib.common.util.array.ArrayStorage;

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
 */
public class AutomatonLStarState<I, D, AI, S> {

    private final GenericObservationTable<I, D> observationTable;
    private final AI hypothesis;
    private final ArrayStorage<StateInfo<S, I>> stateInfos;

    AutomatonLStarState(GenericObservationTable<I, D> observationTable,
                        AI hypothesis,
                        ArrayStorage<StateInfo<S, I>> stateInfos) {
        this.observationTable = observationTable;
        this.hypothesis = hypothesis;
        this.stateInfos = stateInfos;
    }

    GenericObservationTable<I, D> getObservationTable() {
        return observationTable;
    }

    AI getHypothesis() {
        return hypothesis;
    }

    ArrayStorage<StateInfo<S, I>> getStateInfos() {
        return stateInfos;
    }
}
