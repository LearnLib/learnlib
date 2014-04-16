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

package de.learnlib.logging.filter;

import java.util.EnumSet;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import de.learnlib.logging.Category;
import de.learnlib.logging.LearnLogRecord;

/**
 * Filters log messages based on categories. Non-LearnLib log records
 * are filed under category SYSTEM.
 * 
 * @author falkhowar
 */
public class CategoryFilter implements Filter {

    private final EnumSet<Category> categories;
    
    public CategoryFilter(EnumSet<Category> categories) {
        this.categories = categories;
    }
        
    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getClass() != LearnLogRecord.class) {
            return categories.contains(Category.SYSTEM);
        }
        LearnLogRecord lrec = (LearnLogRecord)record;        
        return categories.contains(lrec.getCategory());
   }
    
}
