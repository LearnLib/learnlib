package de.learnlib.filter.statistic.container;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * A stop clock that can be paused and resumed.
 */
class StopClockStatistic extends LearnerStatistic {
    private Instant started;
    private Duration elapsed;

    public StopClockStatistic(String id, @Nullable String description) {
        super(id, description);
        this.elapsed = Duration.ZERO;
        this.started = null;
    }

    public void resume() {
        this.started = Instant.now();
    }

    public void pause() {
        if (started == null) {
            return;
        }
        this.elapsed = this.elapsed.plus(Duration.between(started, Instant.now()));
        this.started = null;
    }

    public Duration getElapsed() {
        return this.elapsed;
    }
}
