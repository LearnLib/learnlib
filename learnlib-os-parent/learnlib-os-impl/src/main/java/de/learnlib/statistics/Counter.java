/* Copyright (C) 2013 TU Dortmund
   This file is part of LearnLib 

   LearnLib is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License version 3.0 as published by the Free Software Foundation.

   LearnLib is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with LearnLib; if not, see
   <http://www.gnu.de/documents/lgpl.en.html>.  */

package de.learnlib.statistics;

/**
 * simple counter.
 *
 * @author falkhowar
 */
public class Counter extends StatisticData {

    private long count = 0;
    
    public Counter(String name, String unit) {
        super(name, unit);
    }
    
    public void increment(long inc) {
        count += inc;
    }
    
    public void increment() {
        count++;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String getSummary() {
        return getName() + " [" + getUnit() + "]: " + count;
    }

    @Override
    public String getDetails() {
        return getSummary();
    }
    
    
}
