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
package de.learnlib.util.statistics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.logging.LearnLogger;
import de.learnlib.filter.statistic.Counter;

/**
 * Very rudimentary profiler.
 */
@ParametersAreNonnullByDefault
public final class SimpleProfiler {

    private static final Map<String, Counter> CUMULATED = new ConcurrentHashMap<>();
    private static final Map<String, Long> PENDING = new ConcurrentHashMap<>();
    private static final LearnLogger LOGGER = LearnLogger.getLogger(SimpleProfiler.class.getName());
    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    private SimpleProfiler() {
        // prevent initialization
    }

    /**
     * Reset internal data.
     */
    public static void reset() {
        CUMULATED.clear();
        PENDING.clear();
    }

    /**
     * Start the timer identified by the given key.
     *
     * @param name
     *         The name of the timer to be started.
     */
    public static void start(String name) {
        PENDING.put(name, System.currentTimeMillis());
    }

    /**
     * Stop the timer identified by the given key. After stopping a timer, the time passed from its
     * {@link #start(String) initialization} will be added to the cumulated time of the specific timer.
     *
     * @param name
     *         The name of the timer to be stopped.
     */
    public static void stop(String name) {
        Long start = PENDING.remove(name);
        if (start == null) {
            return;
        }
        long duration = System.currentTimeMillis() - start;
        Counter sum = CUMULATED.computeIfAbsent(name, k -> new Counter(k, "ms"));
        sum.increment(duration);
    }

    /**
     * Return the counter for the cumulated (passed) time of the given timer.
     *
     * @param name
     *         The name of the timer to be returned.
     *
     * @return The counter for tracking the passed milliseconds of the timer
     */
    public static Counter cumulated(String name) {
        return CUMULATED.get(name);
    }

    /**
     * Get profiling results as string.
     */
    @Nonnull
    public static String getResults() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Counter> e : CUMULATED.entrySet()) {
            sb.append(e.getValue().getSummary())
              .append(", (")
              .append(e.getValue().getCount() / MILLISECONDS_PER_SECOND)
              .append(" s)")
              .append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Log results in category PROFILING.
     */
    public static void logResults() {
        for (Entry<String, Counter> e : CUMULATED.entrySet()) {
            LOGGER.logProfilingInfo(e.getValue());
        }
    }

}
