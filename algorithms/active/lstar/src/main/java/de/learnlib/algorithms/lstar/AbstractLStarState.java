/* Copyright (C) 2013-2019 TU Dortmund
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

import java.io.Serializable;

import de.learnlib.datastructure.observationtable.AbstractObservationTable;

/**
 * Class that contains all data that represent the internal state of the {@link AbstractLStar} learner.
 *
 * @param <I>
 *         The input alphabet type.
 * @param <D>
 *         The output domain type.
 *
 * @author bainczyk
 */
public abstract class AbstractLStarState<I, D> implements Serializable {

    private final AbstractObservationTable<I, D> observationTable;

    AbstractLStarState(final AbstractObservationTable<I, D> observationTable) {
        this.observationTable = observationTable;
    }

    AbstractObservationTable<I, D> getObservationTable() {
        return observationTable;
    }
}
