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

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * A no-op implementation of a {@link StatisticsCollector} that does nothing.
 */
class NoopCollector implements StatisticsCollector {

    @Override
    public Collection<String> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public void clear() {}

    @Override
    public void addText(String id, String description, String text) {}

    @Override
    public Optional<String> getText(String id) {
        return Optional.empty();
    }

    @Override
    public void setFlag(String id, String description, boolean value) {}

    @Override
    public Optional<Boolean> getFlag(String id) {
        return Optional.empty();
    }

    @Override
    public void startOrResumeClock(String id, String description) {}

    @Override
    public void pauseClock(String id) {}

    @Override
    public Optional<Duration> getClock(String id) {
        return Optional.empty();
    }

    @Override
    public void increaseCounter(String id, String description, long increment) {}

    @Override
    public void setCounter(String id, String description, long count) {}

    @Override
    public Optional<Long> getCount(String id) {
        return Optional.empty();
    }

    @Override
    public String printStats() {
        return """
                ################################################
                This is a no-op collector. If you plan on
                collecting statistics, make sure to provide a
                StatisticsProvider service on the classpath.

                A default implementation can be found in the
                statistics module of LearnLib which can be
                included with the following Maven dependency:

                <dependency>
                    <groupId>de.learnlib</groupId>
                    <artifactId>learnlib-statistics</artifactId>
                    <version>${version}</version>
                </dependency>
                ################################################
                """;
    }
}
