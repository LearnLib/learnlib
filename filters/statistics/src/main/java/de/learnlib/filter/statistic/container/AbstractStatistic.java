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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Various types of statistical data to be stored in a StatisticsCollector.
 */
abstract class AbstractStatistic {

    private final String id;
    private final @Nullable String description;

    /**
     * Default constructor.
     *
     * @param id
     *         id of the statistic. Must be unique within the StatisticsCollector.
     * @param description
     *         Optional description of the statistic. If no description is provided, the id is used.
     */
    AbstractStatistic(String id, @Nullable String description) {
        this.id = id;

        if (description == null) {
            this.description = id;
        } else {
            this.description = description;
        }
    }

    public String getId() {
        return id;
    }

    public @Nullable String getDescription() {
        return description;
    }

    protected abstract String renderValue();

    @Override
    public String toString() {
        if (description == null) {
            return id + ": " + renderValue();
        } else {
            return description + ": " + renderValue();
        }
    }
}
