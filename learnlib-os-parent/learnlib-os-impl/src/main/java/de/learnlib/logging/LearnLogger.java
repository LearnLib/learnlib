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
     * remove all handlers of root logger and add a console hander with
     * LLConsoleFormatter instead.
     * 
     * The use of this method is discouraged as it interferes with 
     * (proper) file-based or class-based configuration of logging. 
     * 
     */
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
