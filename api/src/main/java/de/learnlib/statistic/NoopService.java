/* Copyright (C) 2013-2026 TU Dortmund University
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
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A no-op implementation of a {@link StatisticsService} that does nothing.
 */
class NoopService implements StatisticsService {

    @Override
    public Collection<StatisticsKey> getKeys() {
        return Collections.emptyList();
    }

    @Override
    public void clear() {}

    @Override
    public String print() {
        return """
                ################################################
                This is a no-op service. If you plan on
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

    @Override
    public void setText(StatisticsKey key, String text, @Nullable Object owner) {}

    @Override
    public Optional<String> getText(StatisticsKey key, @Nullable Object owner) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, String> getTexts(StatisticsKey key) {
        return Collections.emptyMap();
    }

    @Override
    public void setFlag(StatisticsKey key, boolean value, @Nullable Object owner) {}

    @Override
    public Optional<Boolean> getFlag(StatisticsKey key, @Nullable Object owner) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, Boolean> getFlags(StatisticsKey key) {
        return Collections.emptyMap();
    }

    @Override
    public void startOrResumeClock(StatisticsKey key, @Nullable Object owner) {}

    @Override
    public void pauseClock(StatisticsKey key, @Nullable Object owner) {}

    @Override
    public Optional<Duration> getClock(StatisticsKey key, @Nullable Object owner) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, Duration> getClocks(StatisticsKey key) {
        return Collections.emptyMap();
    }

    @Override
    public void increaseCounter(StatisticsKey key, long increment, @Nullable Object owner) {}

    @Override
    public void setCounter(StatisticsKey key, long value, @Nullable Object owner) {}

    @Override
    public Optional<Long> getCount(StatisticsKey key, @Nullable Object owner) {
        return Optional.empty();
    }

    @Override
    public Map<Integer, Long> getCounts(StatisticsKey key) {
        return Collections.emptyMap();
    }
}
