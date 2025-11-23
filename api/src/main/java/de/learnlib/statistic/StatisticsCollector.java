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
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A container that collects various statistics of different types. Individual measurements are identified via an
 * {@code id} which can be optionally enhanced by a description for the purpose of pretty-printing. Implementations may
 * allow for key collisions as a means to merge statistics from different components.
 * <p>
 * Note that implementations of this interface should be thread-safe as instances of the same collector may be passed
 * across multiple threads.
 */
public interface StatisticsCollector {

    // General

    /**
     * Returns all registered keys of this collector. May be used when exporting data.
     *
     * @return the keys for which any data as been collected.
     */
    Collection<String> getKeys();

    /**
     * Clears the data of this collector. May be used when wanting to start a fresh data collection.
     */
    void clear();

    // Generic text

    /**
     * Stores the provided text for the given id.
     *
     * @param id
     *         the id of the text
     * @param description
     *         description of the data, e.g., "configuration"
     * @param text
     *         the text to be stored
     */
    void addText(String id, @Nullable String description, String text);

    /**
     * Retrieves the text with the given id.
     *
     * @param id
     *         the id of the text
     *
     * @return the stored text, or {@link Optional#empty()} if no text for this id exists
     */
    Optional<String> getText(String id);

    // Boolean flags

    /**
     * Stores the provided boolean for the given id.
     *
     * @param id
     *         the id of the flag
     * @param description
     *         description of the boolean, e.g., "accurate"
     * @param value
     *         the boolean value to be stored
     */
    void setFlag(String id, @Nullable String description, boolean value);

    /**
     * Retrieves the flag with the given id.
     *
     * @param id
     *         the id of the flag
     *
     * @return the stored boolean, or {@link Optional#empty()} if no boolean for this id exists
     */
    Optional<Boolean> getFlag(String id);

    // Time

    /**
     * Starts the clock with the given id. If there is already a clock with this id, it is resumed.
     *
     * @param id
     *         the id of the clock
     * @param description
     *         description of the clock, e.g., "learning time"
     */
    void startOrResumeClock(String id, @Nullable String description);

    /**
     * Pauses the clock with the given id. If there is no clock with this id, nothing happens.
     *
     * @param id
     *         the id of the clock
     */
    void pauseClock(String id);

    /**
     * Returns the current value of the clock with the given id.
     *
     * @param id
     *         the id of the clock
     *
     * @return the current value of the clock, or {@link Optional#empty()} if no clock for this id exists
     */
    Optional<Duration> getClock(String id);

    // Counter

    /**
     * Increases the counter with the given id. If no counter with this id exists, it is created.
     *
     * @param id
     *         the id of the counter
     * @param description
     *         description of the counter, e.g., "number of rounds"
     */
    default void increaseCounter(String id, @Nullable String description) {
        increaseCounter(id, description, 1);
    }

    /**
     * Increases the counter with the given id by the provided increment. If no counter with this id exists, it is
     * created and initialized with the provided increment.
     *
     * @param id
     *         the id of the counter
     * @param description
     *         description of the counter, e.g., "number of rounds"
     * @param increment
     *         the amount to increase the counter by (must be greater than zero)
     */
    void increaseCounter(String id, String description, long increment);

    /**
     * Sets the counter with the given id to the provided value. If no counter with this id exists, it is created.
     *
     * @param id
     *         the id of the counter
     * @param description
     *         description of the counter, e.g., "number of rounds"
     * @param count
     *         New value for the counter (must be greater than zero)
     */
    void setCounter(String id, @Nullable String description, long count);

    /**
     * Gets the value of the counter with the given id.
     *
     * @param id
     *         the id of the counter
     *
     * @return The value of the counter, or {@link Optional#empty()} if no counter for this id exists
     */
    Optional<Long> getCount(String id);

    /**
     * Returns a string-based representation of the collected statistics.
     *
     * @return a string-based representation of the collected statistics
     */
    String printStats();
}
