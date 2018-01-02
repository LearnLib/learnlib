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

package de.learnlib.api.logging;

/**
 * Categories for markers.
 *
 * @author falkhowar
 */
public enum Category {
    SYSTEM,
    // not LearnLib related
    PHASE,
    // new phase (learning, ce-handling, etc.)
    QUERY,
    // membership query
    COUNTEREXAMPLE,
    // counterexample
    STATISTIC,
    // statistic information
    PROFILING,
    // profiling information
    DATASTRUCTURE,
    // ds maintained by algorithm
    MODEL,
    // inferred hypothesis
    CONFIG,
    // learning setup
    EVENT;
    // splitting of a table cell or similar

    private static final String LEARNLIB_PREFIX = "LEARNLIB";

    public String toMarkerLabel() {
        return LEARNLIB_PREFIX + '_' + name();
    }
}
