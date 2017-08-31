/* Copyright (C) 2013-2017 TU Dortmund
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

package de.learnlib.statistics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.logging.LearnLogger;

/**
 * Very rudimentary profiler.
 */
@ParametersAreNonnullByDefault
public final class SimpleProfiler {

    private static final Map<String, Counter> CUMULATED = new ConcurrentHashMap<>();
    private static final Map<String, Long> PENDING = new ConcurrentHashMap<>();
    private static boolean PROFILE = true;
    private static final LearnLogger LOGGER = LearnLogger.getLogger(SimpleProfiler.class.getName());
    private static final double MILLISECONDS_PER_SECOND = 1000.0;

    private SimpleProfiler() {
        // prevent initialization
    }

    /**
     * reset internal data.
     */
    public static void reset() {
        CUMULATED.clear();
        PENDING.clear();
    }

    /**
     * start activity.
     */
    public static void start(String name) {
        if (!PROFILE) {
            return;
        }
        long start = System.currentTimeMillis();

        PENDING.put(name, start);

    }

    /**
     * stop activity.
     */
    public static void stop(String name) {
        if (!PROFILE) {
            return;
        }
        Long start = PENDING.remove(name);
        if (start == null) {
            return;
        }
        long duration = System.currentTimeMillis() - start;
        Counter sum = CUMULATED.get(name);
        if (sum == null) {
            sum = new Counter(name, "ms");
        }
        sum.increment(duration);
        CUMULATED.put(name, sum);
    }

    /**
     * get profiling results as string.
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
     * log results in category PROFILING.
     */
    public static void logResults() {
        for (Entry<String, Counter> e : CUMULATED.entrySet()) {
            LOGGER.logProfilingInfo(e.getValue());
        }
    }

}
