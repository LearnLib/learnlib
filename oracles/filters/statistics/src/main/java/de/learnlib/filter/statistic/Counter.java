/* Copyright (C) 2013-2018 TU Dortmund
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

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * simple counter.
 *
 * @author falkhowar
 */
@ParametersAreNonnullByDefault
public class Counter extends AbstractStatisticData {

    private final AtomicLong count = new AtomicLong(0L);

    public Counter(String name, String unit) {
        super(name, unit);
    }

    public void increment(long inc) {
        count.addAndGet(inc);
    }

    public void increment() {
        count.incrementAndGet();
    }

    public long getCount() {
        return count.get();
    }

    @Override
    @Nonnull
    public String toString() {
        return getDetails();
    }

    @Override
    @Nonnull
    public String getSummary() {
        return getName() + " [" + getUnit() + "]: " + count;
    }

    @Override
    @Nonnull
    public String getDetails() {
        return getSummary();
    }

}
