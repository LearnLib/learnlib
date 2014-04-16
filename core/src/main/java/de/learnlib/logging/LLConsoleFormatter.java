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

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Extends SimpleFormatter to include categories in output.
 * 
 * @author falkhowar
 */
public class LLConsoleFormatter extends SimpleFormatter {
        
    protected LLConsoleFormatter() {
    }

    @Override
    public String format(LogRecord record) {
        String formatted = super.format(record);
        String category = "SYSTEM";
        if (record.getClass() == LearnLogRecord.class) {
            LearnLogRecord lrec = (LearnLogRecord)record;
            category = lrec.getCategory().toString();
        }
        formatted = formatted.replaceFirst( record.getLevel().getName() + ":", 
            record.getLevel().getName() + " [" + category + "]:");                    
        return formatted;
    }
}
