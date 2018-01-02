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

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A simple histogram data set.
 *
 * @author falkhowar
 */
@ParametersAreNonnullByDefault
public class HistogramDataSet extends AbstractStatisticData {

    private final SortedMap<Long, Integer> histogram = new TreeMap<>();

    private long size;

    private long sum;

    private double mean;

    public HistogramDataSet(String name, String unit) {
        super(name, unit);
    }

    public void addDataPoint(Long value) {
        Integer i = histogram.get(value);
        if (i == null) {
            i = 0;
        }
        histogram.put(value, i + 1);
        sum += value;
        size++;
        mean = mean + ((((double) value) - mean) / size);
    }

    @Nonnull
    public SortedMap<Long, Integer> getHistogram() {
        return histogram;
    }

    public double getMean() {
        return mean;
    }

    public long getSize() {
        return size;
    }

    public long getSum() {
        return sum;
    }

    public double getMedian() {
        long idx = 0;
        for (Entry<Long, Integer> e : histogram.entrySet()) {
            int count = e.getValue();
            idx += count;
            if (idx >= size / 2) {
                return e.getValue();
            }
        }
        return 0.0;
    }

    @Override
    @Nonnull
    public String getSummary() {
        return getName() + " [" + getUnit() + "]: " + size + " (count), " + sum + " (sum), " + mean + " (mean), " +
               getMedian() + " (median)";
    }

    @Override
    @Nonnull
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append(System.getProperty("line.separator"));
        for (Entry<Long, Integer> e : histogram.entrySet()) {
            sb.append("    ").append(e.getKey()).
                    append(", ").append(e.getValue()).
                      append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

}
