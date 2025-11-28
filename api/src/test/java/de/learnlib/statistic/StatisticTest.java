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
package de.learnlib.statistic;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StatisticTest {

    @Test
    public void testSingleton() {
        StatisticsCollector col1 = Statistics.getCollector();
        StatisticsCollector col2 = Statistics.getCollector();

        Assert.assertSame(col1, col2);
    }

    @Test
    public void testNoop() {
        StatisticsCollector collector = Statistics.getCollector();

        String id1 = "id1";
        collector.addText(id1, null, "text");
        Assert.assertTrue(collector.getText(id1).isEmpty());

        String id2 = "id2";
        collector.setFlag(id2, null, true);
        Assert.assertTrue(collector.getFlag(id2).isEmpty());

        String id3 = "id3";
        collector.startOrResumeClock(id3, null);
        collector.pauseClock(id3);
        Assert.assertTrue(collector.getClock(id3).isEmpty());

        String id4 = "id4";
        collector.increaseCounter(id4, null);
        Assert.assertTrue(collector.getCount(id4).isEmpty());

        Assert.assertTrue(collector.getKeys().isEmpty());

        // assert no throws
        collector.clear();

        Assert.assertNotNull(collector.printStats());
    }
}
