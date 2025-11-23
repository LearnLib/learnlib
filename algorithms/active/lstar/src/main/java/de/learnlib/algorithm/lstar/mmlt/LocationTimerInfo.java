package de.learnlib.algorithm.lstar.mmlt;

import net.automatalib.symbol.time.TimedInput;
import net.automatalib.automaton.mmlt.TimerInfo;
import net.automatalib.word.Word;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Stores information about local timers of a location.
 *
 * @param <I> Input type for non-delaying inputs
 * @param <O> Output symbol type
 */
public class LocationTimerInfo<I, O> implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LocationTimerInfo.class);

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
     */
    public void addTimer(TimerInfo<?, O> timer) {
        this.timers.put(timer.name(), timer);
        this.sortedTimers.add(timer);
        this.sortedTimers.sort(Comparator.comparingLong(TimerInfo::initial));
    }

    public void removeTimer(String timerName) {
        if (!this.timers.containsKey(timerName)) {
            logger.warn("Attempted to remove an unknown timer.");
            return;
        }
        TimerInfo<?, O> removedTimer = this.timers.remove(timerName);
        this.sortedTimers.remove(removedTimer);
    }

    @Nullable
    public TimerInfo<?, O> getTimerInfo(long initial) {
        Optional<TimerInfo<?, O>> timer = this.sortedTimers.stream().filter(t -> t.initial() == initial).findAny();
        return timer.orElse(null);
    }


    /**
     * Returns the timer with the highest initial value
     *
     * @return Timer with maximum timeout. Null, if no timers defined.
     */
    @Nullable
    public TimerInfo<?, O> getLastTimer() {
        if (this.timers.isEmpty()) {
            return null;
        }
        return sortedTimers.get(sortedTimers.size() - 1);
    }

    /**
     * Sets the given timer to one-shot, ensuring that there is only one one-shot timer at a time.
     * This is preferred over setting the timer property.
     *
     * @param name Name of the new one-shot timer
     */
    public void setOneShotTimer(String name) {
        var oneShotTimer = this.timers.get(name);
        if (oneShotTimer == null) {
            throw new IllegalArgumentException("Unknown one-shot timer name.");
        }
        if (!oneShotTimer.equals(sortedTimers.get(sortedTimers.size() - 1))) {
            throw new IllegalArgumentException("Only the timer with maximum timeout can be one-shot.");
        }

        // update references
        var newTimer = oneShotTimer.asOneShot();
        this.timers.put(name, newTimer);
        this.sortedTimers.set(sortedTimers.size() - 1, newTimer);
    }

    /**
     * Returns a list of all timers defined in this location, sorted by their initial value.
     *
     * @return List of local timers. Empty, if none.
     */
    public List<TimerInfo<?, O>> getSortedTimers() {
        return Collections.unmodifiableList(sortedTimers);
    }

    /**
     * Returns an unmodifiable view of the timers defined for this location.
     * Format: name -> info
     *
     * @return Map of local timers. Empty, if none defined.
     */
    @NonNull
    public Map<String, TimerInfo<?, O>> getLocalTimers() {
        return Collections.unmodifiableMap(this.timers);
    }
}
