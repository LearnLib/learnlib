/* Copyright (C) 2013-2023 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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

package de.learnlib.filter.statistic;

import de.learnlib.api.logging.Category;
import de.learnlib.api.statistic.StatisticData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common interface for statistical data.
 */
public abstract class AbstractStatisticData implements StatisticData {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStatisticData.class);

    private final String name;
    private final String unit;

    protected AbstractStatisticData(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public abstract String getSummary();

    @Override
    public abstract String getDetails();

    public void logData() {
        LOGGER.info(Category.STATISTIC, getDetails());
    }
}
