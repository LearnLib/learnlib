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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.logging.LearnLogger;

/**
 * Very rudimentary profiler. 
 */
@ParametersAreNonnullByDefault
public class SimpleProfiler {

  private static boolean PROFILE = true;
  
  private static final Map<String,Counter> cumulated = new ConcurrentHashMap<>();
  private static final Map<String,Long> pending = new ConcurrentHashMap<>();
   
  private static LearnLogger logger = LearnLogger.getLogger(SimpleProfiler.class.getName());
  
  /**
   * reset internal data.
   */
  public static void reset() {
      cumulated.clear();
      pending.clear();
  }
  
  /**
   * start activity.
   * 
   * @param name 
   */
  public static void start(String name) {
    if (!PROFILE) {
      return;
    }
    long start = System.currentTimeMillis();
  
    pending.put(name,start);
    
  }
  
  /**
   * stop activity.
   * 
   * @param name 
   */
  public static void stop(String name) {
    if (!PROFILE) {
      return;
    }
    Long start = pending.remove(name);
    if (start == null) {
      return;
    }
    long duration = System.currentTimeMillis() - start;
    Counter sum = cumulated.get(name);
    if (sum == null) {
      sum = new Counter(name, "ms");
    }
    sum.increment(duration);
    cumulated.put(name, sum);
  }
  
  /**
   * get profiling results as string.
   * 
   * @return 
   */
  @Nonnull
  public static String getResults() {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, Counter> e : cumulated.entrySet()) {
        sb.append(e.getValue().getSummary()).append(", (").append(e.getValue().getCount()/1000.0).
                append(" s)").append(System.lineSeparator());
    }
    return sb.toString();
  }
  
  /**
   * log results in category PROFILING.
   */
  public static void logResults() {
    for (Entry<String, Counter> e : cumulated.entrySet()) {
      logger.logProfilingInfo(e.getValue());
    }  
  }
  
}
