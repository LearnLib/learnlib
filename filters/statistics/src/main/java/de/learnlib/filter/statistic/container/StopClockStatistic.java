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
class StopClockStatistic extends AbstractStatistic {

    private @Nullable Instant started;
    private Duration elapsed;

    StopClockStatistic(String id, @Nullable String description) {
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

    @Override
    public String renderValue() {
        return elapsed.toMillis() + " ms";
    }
}
