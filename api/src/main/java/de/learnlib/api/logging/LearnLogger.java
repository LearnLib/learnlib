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

import de.learnlib.api.statistic.StatisticData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * LearnLib specific logger. Adds some methods to Logger for logging artifacts specific to learning.
 *
 * @author falkhowar
 */
public interface LearnLogger extends Logger {

    /**
     * Convenience method for easing the common practice of using a class name as the name for the logger. Calling this
     * method is equivalent to
     * <pre>
     * LearnLogger.getLogger(clazz.getName())
     * </pre>
     *
     * @param clazz
     *         the class from which to retrieve the name
     *
     * @return the logger for the given class name
     */
    static LearnLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * get an instance of a logger for name. assumes that there is no ordinary logger of the same name.
     */
    static LearnLogger getLogger(String name) {
        return new Slf4jDelegator(LoggerFactory.getLogger(name));
    }

    /**
     * logs a system message at level INFO.
     */
    default void logSystem(String msg) {
        final Marker marker = MarkerFactory.getMarker(Category.SYSTEM.toMarkerLabel());
        info(marker, msg);
    }

    /**
     * logs a learning phase at level INFO.
     */
    default void logPhase(String phase) {
        final Marker marker = MarkerFactory.getMarker(Category.PHASE.toMarkerLabel());
        info(marker, phase);
    }

    /**
     * logs a learning query at level INFO.
     */
    default void logQuery(String phase) {
        final Marker marker = MarkerFactory.getMarker(Category.QUERY.toMarkerLabel());
        info(marker, phase);
    }

    /**
     * logs setup details.
     */
    default void logConfig(String config) {
        final Marker marker = MarkerFactory.getMarker(Category.CONFIG.toMarkerLabel());
        info(marker, config);
    }

    /**
     * log counterexample.
     */
    default void logCounterexample(String ce) {
        final Marker marker = MarkerFactory.getMarker(Category.COUNTEREXAMPLE.toMarkerLabel());
        info(marker, ce);
    }

    /**
     * logs an event. E.g., creation of new table row.
     */
    default void logEvent(String desc) {
        final Marker marker = MarkerFactory.getMarker(Category.EVENT.toMarkerLabel());
        info(marker, desc);
    }

    /**
     * log a piece of profiling info.
     */
    default void logProfilingInfo(StatisticData profiling) {
        final Marker marker = MarkerFactory.getMarker(Category.PROFILING.toMarkerLabel());
        info(marker, profiling.getSummary());
    }

    /**
     * log statistic info.
     */
    default void logStatistic(StatisticData statistics) {
        final Marker marker = MarkerFactory.getMarker(Category.STATISTIC.toMarkerLabel());
        info(marker, statistics.getSummary());
    }

    /**
     * log a model.
     */
    default void logModel(Object o) {
        final Marker marker = MarkerFactory.getMarker(Category.MODEL.toMarkerLabel());
        info(marker, o.toString());
    }

    /**
     * log a data structure.
     */
    default void logDataStructure(Object o) {
        final Marker marker = MarkerFactory.getMarker(Category.DATASTRUCTURE.toMarkerLabel());
        info(marker, o.toString());
    }

}
