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
package de.learnlib.statistic;

import java.util.ServiceLoader;

/**
 * Factory for obtaining {@link StatisticsService}s.
 */
public final class Statistics {

    private static final StatisticsProvider PROVIDER;

    static {
        final ServiceLoader<StatisticsProvider> loader = ServiceLoader.load(StatisticsProvider.class);

        StatisticsProvider bestProvider = new NoopProvider();
        for (StatisticsProvider sp : loader) {
            if (sp.getPriority() > bestProvider.getPriority()) {
                bestProvider = sp;
            }
        }

        PROVIDER = bestProvider;
    }

    private Statistics() {
        // prevent instantiation
    }

    /**
     * Returns a {@link StatisticsService} for collecting statistics. Note that the returned instances should behave
     * as "per-thread-singletons", i.e., within a thread, the same instance should be returned as to enable client-code
     * to collect statistics over various invocations across different components. However, in a multi-threaded
     * benchmark scenario, each thread should obtain its own instance.
     *
     * @return the service
     */
    public static StatisticsService getService() {
        return PROVIDER.getService();
    }

    private static final class NoopProvider implements StatisticsProvider {

        private static final StatisticsService SERVICE = new NoopService();

        @Override
        public int getPriority() {
            return Integer.MIN_VALUE;
        }

        @Override
        public StatisticsService getService() {
            return SERVICE;
        }
    }
}
