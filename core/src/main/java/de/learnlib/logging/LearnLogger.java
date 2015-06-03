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

import de.learnlib.statistics.StatisticData;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * LearnLib specific logger. Adds some methods to Logger 
 * for logging artifacts specific to learning. 
 * 
 * @author falkhowar
 */
public class LearnLogger extends Logger {
 
    private LearnLogger(String name) {
        super(name,null);
    }
    
    
                    
    /**
     * get an instance of a logger for name. assumes that there is 
     * no ordinary logger of the same name. 
     * 
     * @param name
     * @return 
     */
    public static LearnLogger getLogger(String name) {
        LogManager m = LogManager.getLogManager();
        Logger log = m.getLogger(name);
        if (log == null) {
            log = new LearnLogger(name);            
            m.addLogger(log);
        }
        return (LearnLogger)log;
    }
    
    /**
     * Convenience method for easing the common practice of using a class name as
     * the name for the logger. Calling this method is equivalent to
     * <pre>
     * LearnLogger.getLogger(clazz.getName())
     * </pre>
     * @param clazz the class from which to retrieve the name
     * @return the logger for the given class name
     */
    public static LearnLogger getLogger(Class<?> clazz) {
    	return getLogger(clazz.getName());
    }
    
    /**
     * remove all handlers of root logger and add a console hander with
     * LLConsoleFormatter instead.
     * 
     * @deprecated The use of this method is discouraged as it interferes with 
     * (proper) file-based or class-based configuration of logging. 
     * 
     */
    @Deprecated
    public static void defaultSetup() {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LLConsoleFormatter());
        Logger logger = Logger.getLogger("");
        for (Handler h : logger.getHandlers()) {
            logger.removeHandler(h);
        }
        logger.addHandler(handler);
    }
    
    /**
     * apply a filter to all handlers of the root logger.
     * 
     * 
     */
    public static void setGlobalFilter(Filter f) {
        Logger logger = Logger.getLogger("");
        for (Handler h : logger.getHandlers()) {
            h.setFilter(f);
        }
    }
    
    /**
     * logs a learning phase at level INFO.
     * 
     * @param phase 
     */
    public void logPhase(String phase) {
        LearnLogRecord rec = new LearnLogRecord(Level.INFO, phase, Category.PHASE);
        this.log(rec);
    }
    
    /**
     * logs a learning query at level INFO.
     * 
     * @param phase 
     */
    public void logQuery(String phase) {
        LearnLogRecord rec = new LearnLogRecord(Level.INFO, phase, Category.QUERY);
        this.log(rec);
    }
    
    /**
     * logs setup details
     * 
     * @param config 
     */
    public void logConfig(String config) {
        LearnLogRecord rec = new LearnLogRecord(Level.INFO, config, Category.CONFIG);
        this.log(rec);        
    }

    /**
     * log counterexample 
     * 
     * @param ce 
     */
    public void logCounterexample(String ce) {    
        LearnLogRecord rec = new LearnLogRecord(Level.INFO, ce, Category.COUNTEREXAMPLE);
        this.log(rec);          
    }
        
    /**
     * logs an event. E.g., creation of new table row 
     * 
     * @param desc 
     */
    public void logEvent(String desc) {
        LearnLogRecord rec = new LearnLogRecord(Level.INFO, desc, Category.EVENT);
        this.log(rec);
    }
    
    /**
     * log a piece of profiling info
     * 
     * @param profiling 
     */
    public void logProfilingInfo(StatisticData profiling) {
        LearnLogRecord rec = new StatisticLogRecord(Level.INFO, profiling, Category.PROFILING);
        this.log(rec);
    }
    
    /**
     * log statistic info
     * 
     * @param statistics 
     */
    public void logStatistic(StatisticData statistics) {
        LearnLogRecord rec = new StatisticLogRecord(Level.INFO, statistics, Category.STATISTIC);
        this.log(rec);        
    }

    /**
     * log a model
     * 
     * @param o 
     */
    public void logModel(Object o) {
        LearnLogRecord rec = new PlottableLogRecord(Level.INFO, o, Category.MODEL);
        this.log(rec);        
    }
    
    /**
     * log a data structure
     * 
     * @param o 
     */
    public void logDataStructure(Object o) {
        LearnLogRecord rec = new PlottableLogRecord(Level.INFO, o, Category.DATASTRUCTURE);
        this.log(rec);        
    }
    
}
