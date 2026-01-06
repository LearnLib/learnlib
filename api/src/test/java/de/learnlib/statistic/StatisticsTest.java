/* Copyright (C) 2013-2026 TU Dortmund University
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

public class StatisticsTest {

    @Test
    public void testSingleton() {
        StatisticsService s1 = Statistics.getService();
        StatisticsService s2 = Statistics.getService();

        Assert.assertSame(s1, s2);
    }

    @Test
    public void testSubKeys() {

        StatisticsKey key1 = new StatisticsKey("key1");
        StatisticsKey key1Desc = new StatisticsKey("key1", "description");
        StatisticsKey key1id1 = key1.withId("id1");
        StatisticsKey key1id2 = key1.withId("id2");
        StatisticsKey key2 = new StatisticsKey("key2");

        Assert.assertTrue(key1.isSubkeyOf(key1));
        Assert.assertTrue(key1.isSubkeyOf(key1Desc));
        Assert.assertTrue(key1Desc.isSubkeyOf(key1));
        Assert.assertTrue(key1.isSubkeyOf(key1id1));
        Assert.assertFalse(key1id1.isSubkeyOf(key1));
        Assert.assertTrue(key1.isSubkeyOf(key1id2));
        Assert.assertFalse(key1id2.isSubkeyOf(key1));
        Assert.assertFalse(key1id1.isSubkeyOf(key1id2));
        Assert.assertFalse(key1id2.isSubkeyOf(key1id1));
        Assert.assertFalse(key1.isSubkeyOf(key2));
        Assert.assertFalse(key2.isSubkeyOf(key1));
    }

    @Test
    public void testNoop() {
        StatisticsService stats = Statistics.getService();

        StatisticsKey key1 = new StatisticsKey("key1");
        stats.setText(key1, "text");
        Assert.assertTrue(stats.getText(key1).isEmpty());

        StatisticsKey key2 = new StatisticsKey("key2");
        stats.setFlag(key2, true);
        Assert.assertTrue(stats.getFlag(key2).isEmpty());

        StatisticsKey key3 = new StatisticsKey("key3");
        stats.startOrResumeClock(key3);
        stats.pauseClock(key3);
        Assert.assertTrue(stats.getClock(key3).isEmpty());

        StatisticsKey key4 = new StatisticsKey("key4");
        stats.increaseCounter(key4);
        Assert.assertTrue(stats.getCount(key4).isEmpty());

        Assert.assertTrue(stats.getKeys().isEmpty());
        Assert.assertTrue(stats.getTexts(key1).isEmpty());
        Assert.assertTrue(stats.getFlags(key2).isEmpty());
        Assert.assertTrue(stats.getClocks(key3).isEmpty());
        Assert.assertTrue(stats.getCounts(key4).isEmpty());

        // assert no throws
        stats.clear();

        Assert.assertNotNull(stats.print());
    }
}
