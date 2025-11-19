package de.learnlib.statistic;


import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * Interface for a container that stores various statistics during learning.
 */
public interface StatsContainer {

    // Generic text

    /**
     * Stores the provided text for the given id
     * and assigns the provided description.
     *
     * @param id          Text id
     * @param description Description of the data, e.g., "configuration"
     * @param text        The text to be stored
     */
    void addTextInfo(String id, @Nullable String description, String text);

    /**
     * Retrieves the text with the provided id.
     *
     * @param id Id of the text value
     * @return The stored text, or empty if there is no text with this id.
     */
    Optional<String> getTextValue(String id);


    // Boolean flags

    /**
     * Stores the provided boolean for the given id
     * and optionally assigns the provided description.
     *
     * @param id          Flag id
     * @param description Description of the boolean, e.g., "accurate"
     * @param value       The boolean value to be stored
     */
    void setFlag(String id, @Nullable String description, boolean value);

    /**
     * Retrieves the flag with the provided id.
     *
     * @param id Id of the boolean value
     * @return The stored boolean, or empty, if no boolean with this id exists.
     */
    Optional<Boolean> getFlagValue(String id);


    // Time

    /**
     * Starts the clock with the given id and optionally assigns the provided description.
     * If there is already a clock with this id, it is resumed.
     *
     * @param id          Clock id
     * @param description Description of the clock, e.g., "learning time"
     */
    void startOrResumeClock(String id, @Nullable String description);

    /**
     * Pauses the clock with the given id. If there is no clock with this id, nothing happens.
     *
     * @param id Clock id
     */
    void pauseClock(String id);

    /**
     * Returns the current value of the clock with the given id.
     *
     * @param id Clock id
     * @return The current value of the clock, or empty, if no clock with this id exists.
     */
    Optional<Duration> getClockValue(String id);


    // Counter

    /**
     * Increases the counter with the given id and optionally assigns the provided description.
     * If no counter with this id exists, it is created.
     *
     * @param id          Counter id
     * @param description Description of the counter, e.g., "number of rounds"
     */
    default void increaseCounter(String id, @Nullable String description) {
        increaseCounter(id, description, 1);
    }

    /**
     * Increases the counter with the given id by the provided increment
     * and optionally assigns the provided description.
     * If no counter with this id exists, it is created and initialized with the provided increment.
     *
     * @param id          Counter id
     * @param description Description of the counter, e.g., "number of rounds"
     * @param increment   Amount to increase the counter by
     */
    void increaseCounter(String id, @Nullable String description, long increment);

    /**
     * Sets the counter with the given id to the provided value and optionally assigns the provided description.
     * If no counter with this id exists, it is created.
     *
     * @param id          Counter id
     * @param description Description of the counter, e.g., "number of rounds"
     * @param count       New value for the counter. Must be greater than zero.
     */
    void setCounter(String id, @Nullable String description, long count);

    /**
     * Gets the value of the counter with the given id. Returns empty, if no counter with this id exists.
     *
     * @param id Counter id
     * @return The value of the counter, or empty, if no counter with this id exists.
     */
    Optional<Long> getCount(String id);

    /**
     * Prints all stored statistics.
     */
    void printStats();
}
