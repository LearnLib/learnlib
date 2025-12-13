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
import java.time.Instant;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A stop clock that can be paused and resumed.
 */
class ClockContainer implements StatisticContainer {

    private @Nullable Instant started;
    private Duration elapsed;

    ClockContainer() {
        this.elapsed = Duration.ZERO;
        this.started = null;
    }

    void resume() {
        if (this.started != null) {
            throw new IllegalStateException("You cannot resume a timer that is still running");
        }
        this.started = Instant.now();
    }

    void pause() {
        if (started == null) {
            throw new IllegalStateException("You cannot pause a timer that has not been started");
        }
        this.elapsed = this.elapsed.plus(Duration.between(started, Instant.now()));
        this.started = null;
    }

    Duration getElapsed() {
        return this.elapsed;
    }

    @Override
    public String toString() {
        return elapsed.toMillis() + " ms";
    }
}
