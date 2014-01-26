/* Copyright (C) 2013 TU Dortmund
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

package de.learnlib.logging;

import java.util.logging.Level;

import de.learnlib.statistics.StatisticData;

/**
 *
 * @author falkhowar
 */
public class StatisticLogRecord extends LearnLogRecord {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final StatisticData data;
    
    public StatisticLogRecord(Level lvl, StatisticData data, Category category) {
        super(lvl, data.getSummary(), category);
        this.data = data;
    }
 
    public StatisticData getData() {
        return data;
    }
    
}
