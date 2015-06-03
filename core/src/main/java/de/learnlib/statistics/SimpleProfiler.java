/* Copyright (C) 2013-2014 TU Dortmund
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
