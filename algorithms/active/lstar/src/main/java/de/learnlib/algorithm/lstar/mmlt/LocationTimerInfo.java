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
package de.learnlib.algorithm.lstar.mmlt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.symbol.time.TimedInput;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores information about local timers of a location.
 *
 * @param <I>
 *         input symbol type (of non-delaying inputs)
 * @param <O>
 *         output symbol type
 */
public class LocationTimerInfo<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationTimerInfo.class);

    private final Map<String, TimerInfo<?, O>> timers; // name -> info

    // Keep a list of timers sorted by their initial value. This lets us avoid redundant sort operations.
    private final List<TimerInfo<?, O>> sortedTimers;

    private final Word<TimedInput<I>> prefix;

    public LocationTimerInfo(Word<TimedInput<I>> prefix) {
        this.prefix = prefix;
        this.timers = new HashMap<>();
        this.sortedTimers = new ArrayList<>();
    }

    public Word<TimedInput<I>> getPrefix() {
        return prefix;
    }

    // ====================

    /**
     * Adds a local timer to this location.
     *
     * @param timer
     *         the timer to add
     *
     */
    public void addTimer(TimerInfo<?, O> timer) {
        this.timers.put(timer.name(), timer);
        this.sortedTimers.add(timer);
        this.sortedTimers.sort(Comparator.comparingLong(TimerInfo::initial));
    }

    public void removeTimer(String timerName) {
        final TimerInfo<?, O> removedTimer = this.timers.remove(timerName);
        if (removedTimer == null) {
            LOGGER.warn("Attempted to remove an unknown timer.");
        } else {
            this.sortedTimers.remove(removedTimer);
        }
    }

    /**
     * Returns the timer with the given initial value.
     *
     * @param initial
     *         the queried initial value
     *
     * @return the timer with given timeout, {@code null} if no such timer exists
     */
    public @Nullable TimerInfo<?, O> getTimerInfo(long initial) {
        for (TimerInfo<?, O> t : this.sortedTimers) {
            if (t.initial() == initial) {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns the timer with the highest initial value.
     *
     * @return the timer with maximum timeout, {@code null} if no timers defined
     */
    public @Nullable TimerInfo<?, O> getLastTimer() {
        if (this.timers.isEmpty()) {
            return null;
        }
        return sortedTimers.get(sortedTimers.size() - 1);
    }

    /**
     * Sets the given timer to one-shot, ensuring that there is only one one-shot timer at a time. This is preferred
     * over setting the timer property.
     *
     * @param name
     *         the name of the new one-shot timer
     */
    public void setOneShotTimer(String name) {
        TimerInfo<?, O> oneShotTimer = this.timers.get(name);
        if (oneShotTimer == null) {
            throw new IllegalArgumentException("Unknown one-shot timer name.");
        }
        if (!oneShotTimer.equals(sortedTimers.get(sortedTimers.size() - 1))) {
            throw new IllegalArgumentException("Only the timer with maximum timeout can be one-shot.");
        }

        // update references
        TimerInfo<?, O> newTimer = oneShotTimer.asOneShot();
        this.timers.put(name, newTimer);
        this.sortedTimers.set(sortedTimers.size() - 1, newTimer);
    }

    /**
     * Returns an unmodifiable list of all timers defined in this location, sorted by their initial value.
     *
     * @return list of local timers, may be empty
     */
    public List<TimerInfo<?, O>> getSortedTimers() {
        return Collections.unmodifiableList(sortedTimers);
    }

    /**
     * Returns an unmodifiable view of the timers defined for this location. Format: name -> info
     *
     * @return map of local timers, may be empty
     */
    public Map<String, TimerInfo<?, O>> getLocalTimers() {
        return Collections.unmodifiableMap(this.timers);
    }
}
