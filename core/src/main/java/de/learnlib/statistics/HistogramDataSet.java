/* Copyright (C) 2013-2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */

package de.learnlib.statistics;

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
public class HistogramDataSet extends StatisticData {

    private SortedMap<Long,Integer> histogram = new TreeMap<>();
    
    private long size = 0;

    private long sum = 0;
    
    private double mean = 0.0;
        
    public HistogramDataSet(String name, String unit) {
        super(name, unit);
    }
           
    public void addDataPoint(Long value) {
        Integer i = histogram.get(value);
        if (i == null) {
            i = 0;
        }
        histogram.put(value, i+1);
        sum += value;
        size++;        
        mean = mean + ((((double) value) - mean) / size);                
    }
    
    
    @Nonnull
    public SortedMap<Long, Integer> getHistogram() {
        return histogram;
    }
    
    public double getMedian() {
        long idx = 0;
        for (Entry<Long,Integer> e : histogram.entrySet()) {
            int count = e.getValue();
            idx += count;
            if (idx >= size/2) {
                return e.getValue();
            }
        }
        return 0.0;
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

    @Override
    @Nonnull
    public String getSummary() {
        return getName() + " [" + getUnit() + "]: " + 
                size + " (count), " + 
                sum  + " (sum), " + 
                mean + " (mean), " + 
                getMedian() + " (median)";        
    }

    @Override
    @Nonnull
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary()).append(System.getProperty("line.separator"));
        for (Entry<Long,Integer> e : histogram.entrySet()) {
            sb.append("    ").append(e.getKey()).
                    append(", ").append(e.getValue()).
                    append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    
}
