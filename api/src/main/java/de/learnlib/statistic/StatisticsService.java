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
import java.util.Map;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A service that allows for collecting various statistics of different types. Individual measurements can be identified
 * via {@link StatisticsKey keys} and optional owners.
 * <p>
 * Owners typically are the object instances that write certain data to the service and can be used to resolve
 * collisions in multi-threaded environments. During data recording, it is advised to always provide an owner. By
 * omitting an owner, values for keys are typically overridden / merged (which is what you may want to achieve). During
 * data retrieval, omitting an owner typically aggregates all the recorded data of a given key for convenience.
 * <p>
 * The service also allows one to export all values for a key grouped by their owners. Note that the owners are
 * identified via their {@link System#identityHashCode(Object) identity hash code} as to distinguish between data of
 * {@link Object#equals(Object) equal} objects.
 * <p>
 * <b>Technical detail:</b> implementations of this interface should be thread-safe as instances of the same service
 * may be passed across multiple threads. However, implementations are free to throw exceptions if data collection
 * cannot be synchronized (e.g., starting multiple timers for the same key from different threads).
 */
public interface StatisticsService {

    // General

    /**
     * Returns all registered keys of this collector. May be used for exporting data.
     *
     * @return the keys for which any data has been collected
     */
    Collection<StatisticsKey> getKeys();

    /**
     * Clears the data of this collector. May be used when starting a fresh round of data collection.
     */
    void clear();

    /**
     * Returns a string-based representation of the collected statistics.
     *
     * @return a string-based representation of the collected statistics
     */
    String print();

    // Generic text

    /**
     * Convenience method for {@link #setText(StatisticsKey, String, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key under which to store the text
     * @param text
     *         the text to store
     */
    default void setText(StatisticsKey key, String text) {
        setText(key, text, null);
    }

    /**
     * Stores the provided text for the given key and owner.
     *
     * @param key
     *         the key under which to store the text
     * @param text
     *         the text to store
     * @param owner
     *         the (optional) owner that stores the text
     */
    void setText(StatisticsKey key, String text, @Nullable Object owner);

    /**
     * Convenience method for {@link #getText(StatisticsKey, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key for looking up the text
     *
     * @return the stored text, or {@link Optional#empty()} if no text for this key exists
     */
    default Optional<String> getText(StatisticsKey key) {
        return getText(key, null);
    }

    /**
     * Retrieves the text for the given key, written by the given owner. If {@code owner} is {@code null}, all values
     * for the given key will be aggregated via concatenation.
     *
     * @param key
     *         the key for looking up the text
     * @param owner
     *         the (optional) owner that stored the text
     *
     * @return the stored text, or {@link Optional#empty()} if no text for this key and owner exists
     */
    Optional<String> getText(StatisticsKey key, @Nullable Object owner);

    /**
     * Returns all stored texts for the given key, grouped by their owners. Owners are identified via their
     * {@link System#identityHashCode(Object) identity hash code} as to distinguish between
     * {@link Object#equals(Object) equal} objects.
     *
     * @param key
     *         the key for looking up the text(s)
     *
     * @return a mapping of owner ids to stored texts
     */
    Map<Integer, String> getTexts(StatisticsKey key);

    // Boolean flags

    /**
     * Convenience method for {@link #setFlag(StatisticsKey, boolean, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key under which to store the flag
     * @param value
     *         the flag to store
     */
    default void setFlag(StatisticsKey key, boolean value) {
        setFlag(key, value, null);
    }

    /**
     * Stores the provided boolean flag for the given key and owner.
     *
     * @param key
     *         the key under which to store the flag
     * @param value
     *         the flag to store
     * @param owner
     *         the (optional) owner that stores the flag
     */
    void setFlag(StatisticsKey key, boolean value, @Nullable Object owner);

    /**
     * Convenience method for {@link #getFlag(StatisticsKey, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key for looking up the flag
     *
     * @return the stored flag, or {@link Optional#empty()} if no flag for this key exists
     */
    default Optional<Boolean> getFlag(StatisticsKey key) {
        return getFlag(key, null);
    }

    /**
     * Retrieves the boolean flag for the given key, written by the given owner. If {@code owner} is {@code null}, all
     * values for the given key will be aggregated via logical disjunction.
     *
     * @param key
     *         the key for looking up the flag
     * @param owner
     *         the (optional) owner that stored the flag
     *
     * @return the stored flag, or {@link Optional#empty()} if no flag for this key and owner exists
     */
    Optional<Boolean> getFlag(StatisticsKey key, @Nullable Object owner);

    /**
     * Returns all stored flags for the given key, grouped by their owners. Owners are identified via their
     * {@link System#identityHashCode(Object) identity hash code} as to distinguish between
     * {@link Object#equals(Object) equal} objects.
     *
     * @param key
     *         the key for looking up the flag(s)
     *
     * @return a mapping of owner ids to stored flags
     */
    Map<Integer, Boolean> getFlags(StatisticsKey key);

    // Time

    /**
     * Convenience method for {@link #startOrResumeClock(StatisticsKey, Object)} that uses {@code null} as
     * {@code owner}.
     *
     * @param key
     *         the key under which to store the clock
     *
     * @throws IllegalStateException
     *         if a clock is resumed that is already running
     */
    default void startOrResumeClock(StatisticsKey key) {
        startOrResumeClock(key, null);
    }

    /**
     * Starts the clock with the given key for the given owner. If there already exists a clock for these coordinates,
     * it is resumed.
     *
     * @param key
     *         the key under which to store the clock
     * @param owner
     *         the (optional) owner that stores the clock
     *
     * @throws IllegalStateException
     *         if a clock is resumed that is already running
     */
    void startOrResumeClock(StatisticsKey key, @Nullable Object owner);

    /**
     * Convenience method for {@link #pauseClock(StatisticsKey, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key under which to store the clock
     *
     * @throws IllegalStateException
     *         if an existing clock is paused that has not been started or resumed yet
     */
    default void pauseClock(StatisticsKey key) {
        pauseClock(key, null);
    }

    /**
     * Pauses the clock with the given key for the given owner. If there is no clock with this key, nothing happens.
     *
     * @param key
     *         the key under which to store the clock
     * @param owner
     *         the (optional) owner that stores the clock
     *
     * @throws IllegalStateException
     *         if an existing clock is paused that has not been started or resumed yet
     */
    void pauseClock(StatisticsKey key, @Nullable Object owner);

    /**
     * Convenience method for {@link #getClock(StatisticsKey, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key for looking up the clock
     *
     * @return the duration of the clock, or {@link Optional#empty()} if no clock for this key exists
     */
    default Optional<Duration> getClock(StatisticsKey key) {
        return getClock(key, null);
    }

    /**
     * Returns the current value of the clock with the given key for the given owner. If {@code owner} is {@code null},
     * all values for the given key will be aggregated via {@link Duration#plus(Duration) addition}.
     * <p>
     * Note that clocks need to be {@link #pauseClock(StatisticsKey, Object) paused} before the duration of elapsed time
     * is available.
     *
     * @param key
     *         the key for looking up the clock
     * @param owner
     *         the (optional) owner that stored the clock
     *
     * @return the duration of the clock, or {@link Optional#empty()} if no clock for this key and owner exists
     */
    Optional<Duration> getClock(StatisticsKey key, @Nullable Object owner);

    /**
     * Returns all stored clocks for the given key, grouped by their owners. Owners are identified via their
     * {@link System#identityHashCode(Object) identity hash code} as to distinguish between
     * {@link Object#equals(Object) equal} objects.
     *
     * @param key
     *         the key for looking up the clock(s)
     *
     * @return a mapping of owner ids to clock values
     */
    Map<Integer, Duration> getClocks(StatisticsKey key);

    // Counter

    /**
     * Convenience method for {@link #increaseCounter(StatisticsKey, long)} that uses {@code 1} as {@code value}.
     *
     * @param key
     *         the key under which to store the counter
     */
    default void increaseCounter(StatisticsKey key) {
        increaseCounter(key, 1);
    }

    /**
     * Convenience method for {@link #increaseCounter(StatisticsKey, long, Object)} that uses {@code 1} as
     * {@code value}.
     *
     * @param key
     *         the key under which to store the counter
     * @param owner
     *         the (optional) owner that stores the counter
     */
    default void increaseCounter(StatisticsKey key, @Nullable Object owner) {
        increaseCounter(key, 1, owner);
    }

    /**
     * Convenience method for {@link #increaseCounter(StatisticsKey, long, Object)} that uses {@code null} as
     * {@code owner}.
     *
     * @param key
     *         the key under which to store the counter
     * @param increment
     *         the value by which to increment the counter
     */
    default void increaseCounter(StatisticsKey key, long increment) {
        increaseCounter(key, increment, null);
    }

    /**
     * Increases the counter with the given key for the given owner by the provided increment. If no counter with this
     * key exists, it is created and initialized with the provided increment.
     *
     * @param key
     *         the key under which to store the counter
     * @param increment
     *         the value by which to increment the counter
     * @param owner
     *         the (optional) owner that stores the counter
     */
    void increaseCounter(StatisticsKey key, long increment, @Nullable Object owner);

    /**
     * Convenience method for {@link #setCounter(StatisticsKey, long, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key under which to store the counter
     * @param value
     *         the value to set the counter to
     */
    default void setCounter(StatisticsKey key, long value) {
        setCounter(key, value, null);
    }

    /**
     * Sets the counter with the given key to the provided value. If no counter with this key exists, it is created.
     *
     * @param key
     *         the key under which to store the counter
     * @param value
     *         the value to set the counter to
     * @param owner
     *         the (optional) owner that stores the counter
     */
    void setCounter(StatisticsKey key, long value, @Nullable Object owner);

    /**
     * Convenience method for {@link #getCount(StatisticsKey, Object)} that uses {@code null} as {@code owner}.
     *
     * @param key
     *         the key for looking up the counter
     *
     * @return the value of the counter, or {@link Optional#empty()} if no counter for this key exists
     */
    default Optional<Long> getCount(StatisticsKey key) {
        return getCount(key, null);
    }

    /**
     * Returns the value of the counter with the given key for the given owner. If {@code owner} is {@code null}, all
     * values for the given key will be aggregated via addition.
     *
     * @param key
     *         the key for looking up the counter
     * @param owner
     *         the (optional) owner that stored the counter
     *
     * @return the value of the counter, or {@link Optional#empty()} if no clock for this key and owner exists
     */
    Optional<Long> getCount(StatisticsKey key, @Nullable Object owner);

    /**
     * Returns all stored counters for the given key, grouped by their owners. Owners are identified via their
     * {@link System#identityHashCode(Object) identity hash code} as to distinguish between
     * {@link Object#equals(Object) equal} objects.
     *
     * @param key
     *         the key for looking up the counter(s)
     *
     * @return a mapping of owner ids to counter values
     */
    Map<Integer, Long> getCounts(StatisticsKey key);
}
