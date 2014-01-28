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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Common interface for statistical data. 
 * 
 * @author falkhowar
 */
@ParametersAreNonnullByDefault
public abstract class StatisticData {
    
    private final String name;
    private final String unit;

    protected StatisticData(String name, String unit) {
        this.name = name;
        this.unit = unit;
    }
        
    @Nonnull
    public String getName() {
        return name;
    }
    
    @Nonnull
    public String getUnit() {
        return unit;
    }
    
    @Nonnull
    abstract public String getSummary();
    
    @Nonnull
    abstract public String getDetails();    
}
