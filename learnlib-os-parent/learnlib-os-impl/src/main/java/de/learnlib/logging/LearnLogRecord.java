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

package de.learnlib.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author falkhowar
 */
public class LearnLogRecord extends LogRecord {
    
    private final Category category;
    
    protected LearnLogRecord(Level lvl, String message, Category category) {
        super(lvl, message);
        this.category = category;
    }
    
    public Category getCategory() {
        return this.category;
    }
    
}
