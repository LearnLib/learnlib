/* Copyright (C) 2013 TU Dortmund
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
