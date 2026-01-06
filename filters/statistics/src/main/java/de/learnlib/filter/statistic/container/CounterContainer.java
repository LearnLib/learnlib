/* Copyright (C) 2013-2026 TU Dortmund University
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

/**
 * A counter that can be increased and set to a particular positive number.
 */
class CounterContainer implements StatisticContainer {

    private long count;

    CounterContainer() {
        this.count = 0;
    }

    void setCount(long count) {
        this.count = count;
    }

    void increase(long increment) {
        this.count += increment;
    }

    long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return Long.toString(count);
    }
}
