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
package de.learnlib.filter.statistic.container;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link StatisticsService} that stores all statistics in a {@link Map}.
 */
public class MapStatisticsService implements StatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapStatisticsService.class);

    private final Map<StatisticsKey, Map<Integer, StatisticContainer>> statistics;
    private final ReentrantReadWriteLock lock;

    public MapStatisticsService() {
        this.statistics = new HashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public Collection<StatisticsKey> getKeys() {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return new HashSet<>(this.statistics.keySet());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            statistics.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String print() {

        final StringBuilder sb = new StringBuilder(125);
        sb.append("""
                          Statistics:
                          ============================================
                          """);

        final ReadLock lock = this.lock.readLock();
        lock.lock();

        try {
            List<Entry<StatisticsKey, Map<Integer, StatisticContainer>>> stats = new ArrayList<>(statistics.entrySet());
            stats.sort(Comparator.comparing(MapStatisticsService::getSortingKey));

            for (Entry<StatisticsKey, Map<Integer, StatisticContainer>> e : stats) {
                sb.append("* ").append(e.getKey());
                Map<Integer, StatisticContainer> map = e.getValue();
                assert !map.isEmpty();
                if (map.size() == 1) {
                    sb.append(": ").append(map.values().iterator().next());
                } else {
                    for (Entry<Integer, StatisticContainer> e2 : map.entrySet()) {
                        sb.append("\n  * Instance ").append(e2.getKey()).append(": ").append(e2.getValue());
                    }
                }
                sb.append('\n');
            }
        } finally {
            lock.unlock();
        }

        sb.append("============================================\n");

        return sb.toString();
    }

    @Override
    public void setText(StatisticsKey key, String text, @Nullable Object owner) {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            setData(key, text, owner, TextContainer.class, TextContainer::new, TextContainer::setText);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<String> getText(StatisticsKey key, @Nullable Object owner) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getData(key, owner, MapStatisticsService::extractText, "", String::concat);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<Integer, String> getTexts(StatisticsKey key) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getDataByOwner(key, MapStatisticsService::extractText);
        } finally {
            lock.unlock();
        }
    }

    private static @Nullable String extractText(StatisticContainer container) {
        if (container instanceof TextContainer text) {
            return text.getText();
        }
        return null;
    }

    @Override
    public void setFlag(StatisticsKey key, boolean value, @Nullable Object owner) {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            setData(key, value, owner, FlagContainer.class, FlagContainer::new, FlagContainer::setFlag);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Boolean> getFlag(StatisticsKey key, @Nullable Object owner) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getData(key, owner, MapStatisticsService::extractFlag, Boolean.FALSE, Boolean::logicalOr);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<Integer, Boolean> getFlags(StatisticsKey key) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getDataByOwner(key, MapStatisticsService::extractFlag);
        } finally {
            lock.unlock();
        }
    }

    private static @Nullable Boolean extractFlag(StatisticContainer container) {
        if (container instanceof FlagContainer flag) {
            return flag.isFlagged();
        }
        return null;
    }

    @Override
    public void startOrResumeClock(StatisticsKey key, @Nullable Object owner) {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            setData(key, null, owner, ClockContainer.class, ClockContainer::new, (inst, val) -> inst.resume());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void pauseClock(StatisticsKey key, @Nullable Object owner) {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            StatisticContainer value =
                    statistics.computeIfAbsent(key, k -> new HashMap<>()).get(System.identityHashCode(owner));
            if (value instanceof ClockContainer clockStatistic) {
                clockStatistic.pause();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Duration> getClock(StatisticsKey key, @Nullable Object owner) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getData(key, owner, MapStatisticsService::extractClock, Duration.ZERO, Duration::plus);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<Integer, Duration> getClocks(StatisticsKey key) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getDataByOwner(key, MapStatisticsService::extractClock);
        } finally {
            lock.unlock();
        }
    }

    private static @Nullable Duration extractClock(StatisticContainer container) {
        if (container instanceof ClockContainer clock) {
            return clock.getElapsed();
        }
        return null;
    }

    @Override
    public void increaseCounter(StatisticsKey key, long increment, @Nullable Object owner) {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            setData(key, increment, owner, CounterContainer.class, CounterContainer::new, CounterContainer::increase);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setCounter(StatisticsKey key, long value, @Nullable Object owner) {
        WriteLock lock = this.lock.writeLock();
        lock.lock();
        try {
            setData(key, value, owner, CounterContainer.class, CounterContainer::new, CounterContainer::setCount);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Long> getCount(StatisticsKey key, @Nullable Object owner) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getData(key, owner, MapStatisticsService::extractCount, 0L, Math::addExact);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<Integer, Long> getCounts(StatisticsKey key) {
        ReadLock lock = this.lock.readLock();
        lock.lock();
        try {
            return getDataByOwner(key, MapStatisticsService::extractCount);
        } finally {
            lock.unlock();
        }
    }

    private static @Nullable Long extractCount(StatisticContainer container) {
        if (container instanceof CounterContainer counter) {
            return counter.getCount();
        }
        return null;
    }

    private <T> Map<Integer, T> getDataByOwner(StatisticsKey key, Function<StatisticContainer, @Nullable T> extractor) {
        final Map<Integer, T> result = new HashMap<>();
        final Map<Integer, StatisticContainer> map = this.statistics.getOrDefault(key, Collections.emptyMap());

        for (Entry<Integer, StatisticContainer> e : map.entrySet()) {
            final T value = extractor.apply(e.getValue());
            if (value != null) {
                result.put(e.getKey(), value);
            }
        }
        return result;
    }

    private <T> Optional<T> getData(StatisticsKey key,
                                    @Nullable Object owner,
                                    Function<StatisticContainer, @Nullable T> extractor,
                                    @NonNull T initialValue,
                                    BinaryOperator<@NonNull T> combiner) {
        Map<Integer, StatisticContainer> stats = statistics.getOrDefault(key, Collections.emptyMap());

        if (stats.isEmpty()) {
            return Optional.empty();
        } else if (owner != null) {
            StatisticContainer container = stats.get(System.identityHashCode(owner));
            if (container != null) {
                T value = extractor.apply(container);
                if (value != null) {
                    return Optional.of(value);
                }
                LOGGER.warn("Value of key '{}' for owner '{}' is not of requested type", key, owner);
            }
        } else {
            if (stats.size() > 1) {
                LOGGER.debug("Key '{}' has been written by objects '{}', aggregating ...", key, stats.keySet());
            }

            @NonNull
            T result = initialValue;
            boolean written = false;

            for (StatisticContainer container : stats.values()) {
                T value = extractor.apply(container);
                if (value != null) {
                    result = combiner.apply(result, value);
                    written = true;
                } else {
                    LOGGER.warn("Value of key '{}' is not of requested type", key);
                }
            }
            return written ? Optional.of(result) : Optional.empty();
        }

        return Optional.empty();
    }

    private <T extends StatisticContainer, V> void setData(StatisticsKey key,
                                                           V value,
                                                           @Nullable Object owner,
                                                           Class<T> clazz,
                                                           Supplier<T> supplier,
                                                           BiConsumer<T, V> processor) {
        StatisticContainer prev = statistics.computeIfAbsent(key, k -> new HashMap<>())
                                            .computeIfAbsent(System.identityHashCode(owner), k -> supplier.get());
        if (clazz.isInstance(prev)) {
            processor.accept(clazz.cast(prev), value);
        } else {
            throw new IllegalArgumentException(
                    "The key '" + key + "' already had a different type of statistic associated with it");
        }
    }

    private static String getSortingKey(Entry<StatisticsKey, ?> entry) {
        StatisticsKey key = entry.getKey();
        String description = key.getDescription();
        return Objects.requireNonNullElseGet(description, key::getKey);
    }

}
