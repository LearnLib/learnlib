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
package de.learnlib.algorithms.adt.learner;

import java.io.Serializable;

import de.learnlib.algorithms.adt.adt.ADT;
import de.learnlib.algorithms.adt.automaton.ADTHypothesis;

/**
 * Utility class that captures all essential state of a {@link ADTLearner} run.
 *
 * @param <S>
 *         hypothesis state type
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author frohme
 */
public class ADTLearnerState<S, I, O> implements Serializable {

    private final ADTHypothesis<I, O> hypothesis;
    private final ADT<S, I, O> adt;

    ADTLearnerState(ADTHypothesis<I, O> hypothesis, ADT<S, I, O> adt) {
        this.hypothesis = hypothesis;
        this.adt = adt;
    }

    ADTHypothesis<I, O> getHypothesis() {
        return hypothesis;
    }

    ADT<S, I, O> getAdt() {
        return adt;
    }

}
