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
package de.learnlib.filter.statistic.container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.learnlib.statistic.StatisticsCollector;

/**
 * A {@link StatisticsCollector} that stores all statistics in a {@link Map}.
 */
@SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel") // quick'n'dirty for now
public class MapStatisticsCollector implements StatisticsCollector {

    private final Map<String, AbstractStatistic> statistics;

    public MapStatisticsCollector() {
        this.statistics = new HashMap<>();
    }

    @Override
    public Collection<String> getKeys() {
        return new HashSet<>(this.statistics.keySet());
    }

    @Override
    public synchronized void clear() {
        statistics.clear();
    }

    @Override
    public synchronized void addText(String id, String description, String text) {
        statistics.put(id, new TextStatistic(id, description, text));
    }

    @Override
    public synchronized Optional<String> getText(String id) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof TextStatistic textStatistic) {
            return Optional.of(textStatistic.getText());
        }
        return Optional.empty();
    }

    @Override
    public synchronized void setFlag(String id, String description, boolean value) {
        statistics.put(id, new FlagStatistic(id, description, value));
    }

    @Override
    public synchronized Optional<Boolean> getFlag(String id) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof FlagStatistic flagStatistic) {
            return Optional.of(flagStatistic.isFlagged());
        }
        return Optional.empty();
    }

    @Override
    public synchronized void startOrResumeClock(String id, String description) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof StopClockStatistic clockStatistic) {
            clockStatistic.resume();
        } else {
            // Create and start a new clock:
            StopClockStatistic newClock = new StopClockStatistic(id, description);
            statistics.put(id, newClock);
            newClock.resume();
        }
    }

    @Override
    public synchronized void pauseClock(String id) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof StopClockStatistic clockStatistic) {
            clockStatistic.pause();
        }
    }

    @Override
    public synchronized Optional<Duration> getClock(String id) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof StopClockStatistic clockStatistic) {
            return Optional.of(clockStatistic.getElapsed());
        }
        return Optional.empty();
    }

    @Override
    public synchronized void increaseCounter(String id, String description, long increment) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof CounterStatistic counterStatistic) {
            counterStatistic.increase(increment);
        } else {
            // Create a new counter:
            setCounter(id, description, increment);
        }
    }

    @Override
    public synchronized void setCounter(String id, String description, long count) {
        statistics.put(id, new CounterStatistic(id, description, count));
    }

    @Override
    public synchronized Optional<Long> getCount(String id) {
        AbstractStatistic value = statistics.get(id);
        if (value instanceof CounterStatistic counterStatistic) {
            return Optional.of(counterStatistic.getCount());
        }
        return Optional.empty();
    }

    // ==================

    @Override
    public synchronized String printStats() {

        List<AbstractStatistic> stats = new ArrayList<>(statistics.values());
        stats.sort(Comparator.comparing(AbstractStatistic::getDescription).thenComparing(AbstractStatistic::getId));

        final StringBuilder sb = new StringBuilder(125);

        sb.append("Statistics:\n============================================\n");

        for (AbstractStatistic stat : stats) {
            sb.append("* ").append(stat).append('\n');
        }

        sb.append("============================================\n");

        return sb.toString();
    }

}
