package de.learnlib.statistic.container;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * A dummy implementation of {@link StatsContainer} that does nothing.
 */
public class DummyStatsContainer implements StatsContainer {
    @Override
    public void addTextInfo(String id, @Nullable String description, String text) {

    }

    @Override
    public Optional<String> getTextValue(String id) {
        return Optional.empty();
    }

    @Override
    public void setFlag(String id, @Nullable String description, boolean value) {

    }

    @Override
    public Optional<Boolean> getFlagValue(String id) {
        return Optional.empty();
    }

    @Override
    public void startOrResumeClock(String id, @Nullable String description) {

    }

    @Override
    public void pauseClock(String id) {

    }

    @Override
    public Optional<Duration> getClockValue(String id) {
        return Optional.empty();
    }

    @Override
    public void increaseCounter(String id, @Nullable String description, long increment) {

    }

    @Override
    public void setCounter(String id, @Nullable String description, long count) {

    }

    @Override
    public Optional<Long> getCount(String id) {
        return Optional.empty();
    }

    @Override
    public void printStats() {
        System.out.println("Dummy container");
    }
}
