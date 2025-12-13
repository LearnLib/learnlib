/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
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
package de.learnlib.filter.statistic.container;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsService;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MapStatisticsProviderTest {

    @Test
    public void testSingleton() {
        StatisticsService s1 = Statistics.getService();
        StatisticsService s2 = Statistics.getService();

        Assert.assertSame(s1, s2);
    }

    @Test
    public void testPerThread() throws ExecutionException, InterruptedException, TimeoutException {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        StatisticsService s1 = pool.submit(Statistics::getService).get(10, TimeUnit.SECONDS);
        StatisticsService s2 = pool.submit(Statistics::getService).get(10, TimeUnit.SECONDS);

        Assert.assertNotSame(s1, s2);
        pool.shutdown();
    }
}
