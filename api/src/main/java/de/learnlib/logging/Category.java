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

package de.learnlib.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * A set of markers that allow one to categorize logging output.
 */
public final class Category {

    /**
     * Marker for config-related properties.
     */
    public static final Marker CONFIG = toMarker("CONFIG");
    /**
     * Marker for counterexample-related properties.
     */
    public static final Marker COUNTEREXAMPLE = toMarker("COUNTEREXAMPLE");
    /**
     * Marker for data structure-related properties.
     */
    public static final Marker DATASTRUCTURE = toMarker("DATASTRUCTURE");
    /**
     * Marker for event-related properties.
     */
    public static final Marker EVENT = toMarker("EVENT");
    /**
     * Marker for model-related properties.
     */
    public static final Marker MODEL = toMarker("MODEL");
    /**
     * Marker for phase-related properties.
     */
    public static final Marker PHASE = toMarker("PHASE");
    /**
     * Marker for profiling-related properties.
     */
    public static final Marker PROFILING = toMarker("PROFILING");
    /**
     * Marker for query-related properties.
     */
    public static final Marker QUERY = toMarker("QUERY");
    /**
     * Marker for statistic-related properties.
     */
    public static final Marker STATISTIC = toMarker("STATISTIC");
    /**
     * Marker for system-related properties.
     */
    public static final Marker SYSTEM = toMarker("SYSTEM");

    private Category() {
        // prevent instantiation
    }

    private static Marker toMarker(String name) {
        return MarkerFactory.getMarker("LEARNLIB_" + name);
    }

}
