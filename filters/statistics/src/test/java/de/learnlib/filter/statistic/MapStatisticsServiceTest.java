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
package de.learnlib.filter.statistic;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import de.learnlib.statistic.Statistics;
import de.learnlib.statistic.StatisticsKey;
import de.learnlib.statistic.StatisticsService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MapStatisticsServiceTest {

    private static final int SLEEP = 50;

    private static final StatisticsKey KEY_TEXT = new StatisticsKey("key-text");
    private static final StatisticsKey KEY_FLAG = new StatisticsKey("key-flag");
    private static final StatisticsKey KEY_CLOCK = new StatisticsKey("key-clock");
    private static final StatisticsKey KEY_COUNTER = new StatisticsKey("key-counter");

    @BeforeMethod
    public void beforeClass() {
        Statistics.getService().clear();
    }

    @Test
    public void testAggregationText() {
        final Object owner1 = new Object();
        final Object owner2 = new Object();

        final StatisticsService statistics = Statistics.getService();

        final StatisticsKey key1 = KEY_TEXT;
        final StatisticsKey key2 = KEY_TEXT.withId("id1");
        final StatisticsKey key3 = KEY_TEXT.withId("id2");
        final StatisticsKey key4 = new StatisticsKey("key2");

        statistics.setText(key1, "Hello", owner1);
        statistics.setText(key1, "World", owner2);

        statistics.setText(key2, "foo", owner1);
        statistics.setText(key3, "bar", owner2);

        final StatisticsService tester = Statistics.getService();

        // order may be nondeterministic
        Assert.assertEquals(tester.getText(key1).orElseThrow().length(), 10);
        Assert.assertTrue(tester.getText(key1).orElseThrow().contains("Hello"));
        Assert.assertTrue(tester.getText(key1).orElseThrow().contains("World"));

        Assert.assertEquals(tester.getText(key1, owner1).orElseThrow(), "Hello");
        Assert.assertEquals(tester.getText(key1, owner2).orElseThrow(), "World");

        Assert.assertEquals(tester.getText(key2).orElseThrow(), "foo");
        Assert.assertEquals(tester.getText(key3).orElseThrow(), "bar");
        Assert.assertEquals(tester.getText(key2, owner1).orElseThrow(), "foo");
        Assert.assertEquals(tester.getText(key3, owner2).orElseThrow(), "bar");

        Assert.assertEquals(new HashSet<>(tester.getTexts(key1).values()), Set.of("Hello", "World"));
        Assert.assertEquals(tester.getTexts(key2).values(), Collections.singleton("foo"));
        Assert.assertEquals(tester.getTexts(key3).values(), Collections.singleton("bar"));

        Assert.assertTrue(tester.getText(key4).isEmpty());
        Assert.assertTrue(tester.getTexts(key4).isEmpty());
    }

    @Test
    public void testAggregationFlag() {
        final Object owner1 = new Object();
        final Object owner2 = new Object();

        final StatisticsService statistics = Statistics.getService();

        final StatisticsKey key1 = KEY_FLAG;
        final StatisticsKey key2 = KEY_FLAG.withId("id1");
        final StatisticsKey key3 = KEY_FLAG.withId("id2");
        final StatisticsKey key4 = new StatisticsKey("key2");

        statistics.setFlag(key1, true, owner1);
        statistics.setFlag(key1, false, owner2);

        statistics.setFlag(key2, false, owner1);
        statistics.setFlag(key3, false, owner2);

        final StatisticsService tester = Statistics.getService();

        Assert.assertEquals(tester.getFlag(key1).orElseThrow(), true);
        Assert.assertEquals(tester.getFlag(key1, owner1).orElseThrow(), true);
        Assert.assertEquals(tester.getFlag(key1, owner2).orElseThrow(), false);

        Assert.assertEquals(tester.getFlag(key2).orElseThrow(), false);
        Assert.assertEquals(tester.getFlag(key3).orElseThrow(), false);
        Assert.assertEquals(tester.getFlag(key2, owner1).orElseThrow(), false);
        Assert.assertEquals(tester.getFlag(key3, owner2).orElseThrow(), false);

        List<Boolean> flags = new ArrayList<>(tester.getFlags(key1).values());
        flags.sort(Comparator.naturalOrder());
        Assert.assertEquals(flags, Arrays.asList(false, true));
        Assert.assertEquals(tester.getFlags(key2).values(), Collections.singleton(false));
        Assert.assertEquals(tester.getFlags(key3).values(), Collections.singleton(false));

        Assert.assertTrue(tester.getFlag(key4).isEmpty());
        Assert.assertTrue(tester.getFlags(key4).isEmpty());
    }

    @Test
    public void testAggregationClock() throws InterruptedException {
        final Object owner1 = new Object();
        final Object owner2 = new Object();

        final StatisticsService statistics = Statistics.getService();

        final StatisticsKey key1 = KEY_CLOCK;
        final StatisticsKey key2 = KEY_CLOCK.withId("id1");
        final StatisticsKey key3 = KEY_CLOCK.withId("id2");
        final StatisticsKey key4 = new StatisticsKey("key2");

        statistics.startOrResumeClock(key1, owner1);
        statistics.startOrResumeClock(key1, owner2);

        statistics.startOrResumeClock(key2, owner1);
        statistics.startOrResumeClock(key3, owner2);

        // windows has too low of a timer resolution, therefore wait a bit
        Thread.sleep(SLEEP);

        statistics.pauseClock(key1, owner1);
        statistics.pauseClock(key1, owner2);
        statistics.pauseClock(key2, owner1);
        statistics.pauseClock(key3, owner2);

        final StatisticsService tester = Statistics.getService();

        final Duration c11 = tester.getClock(key1, owner1).orElseThrow();
        final Duration c12 = tester.getClock(key1, owner2).orElseThrow();

        Assert.assertTrue(c11.compareTo(Duration.ZERO) > 0);
        Assert.assertTrue(c12.compareTo(Duration.ZERO) > 0);
        Assert.assertEquals(tester.getClock(key1).orElseThrow(), c11.plus(c12));

        final Duration c21 = tester.getClock(key2, owner1).orElseThrow();
        final Duration c32 = tester.getClock(key3, owner2).orElseThrow();

        Assert.assertTrue(c21.compareTo(Duration.ZERO) > 0);
        Assert.assertTrue(c32.compareTo(Duration.ZERO) > 0);
        Assert.assertEquals(tester.getClock(key2, owner1).orElseThrow(), c21);
        Assert.assertEquals(tester.getClock(key3, owner2).orElseThrow(), c32);

        Assert.assertEquals(new HashSet<>(tester.getClocks(key1).values()), new HashSet<>(Arrays.asList(c11, c12)));
        Assert.assertEquals(tester.getClocks(key2).values(), Collections.singleton(c21));
        Assert.assertEquals(tester.getClocks(key3).values(), Collections.singleton(c32));

        Assert.assertTrue(tester.getClock(key4).isEmpty());
        Assert.assertTrue(tester.getClocks(key4).isEmpty());
    }

    @Test
    public void testAggregationCounter() {
        final Object owner1 = new Object();
        final Object owner2 = new Object();

        final StatisticsService statistics = Statistics.getService();

        final StatisticsKey key1 = KEY_COUNTER;
        final StatisticsKey key2 = KEY_COUNTER.withId("id1");
        final StatisticsKey key3 = KEY_COUNTER.withId("id2");
        final StatisticsKey key4 = new StatisticsKey("key2");

        statistics.increaseCounter(key1, 2, owner1);
        statistics.increaseCounter(key1, 3, owner2);

        statistics.setCounter(key2, 23, owner1);
        statistics.setCounter(key3, 42, owner2);

        final StatisticsService tester = Statistics.getService();

        Assert.assertEquals(tester.getCount(key1).orElseThrow(), 5);
        Assert.assertEquals(tester.getCount(key1, owner1).orElseThrow(), 2);
        Assert.assertEquals(tester.getCount(key1, owner2).orElseThrow(), 3);

        Assert.assertEquals(tester.getCount(key2).orElseThrow(), 23);
        Assert.assertEquals(tester.getCount(key3).orElseThrow(), 42);
        Assert.assertEquals(tester.getCount(key2, owner1).orElseThrow(), 23);
        Assert.assertEquals(tester.getCount(key3, owner2).orElseThrow(), 42);

        Assert.assertEquals(new HashSet<>(tester.getCounts(key1).values()), Set.of(2L, 3L));
        Assert.assertEquals(new HashSet<>(tester.getCounts(key2).values()), Set.of(23L));
        Assert.assertEquals(new HashSet<>(tester.getCounts(key3).values()), Set.of(42L));

        Assert.assertTrue(tester.getCount(key4).isEmpty());
        Assert.assertTrue(tester.getCounts(key4).isEmpty());
    }

    @Test
    public void testInsertRetrievalText() {

        final StatisticsService statistics = Statistics.getService();

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());

        statistics.setText(KEY_TEXT, "Hello World!");
        Assert.assertEquals(statistics.getText(KEY_TEXT).orElseThrow(), "Hello World!");

        statistics.setText(KEY_TEXT, "Test");
        Assert.assertEquals(statistics.getText(KEY_TEXT).orElseThrow(), "Test");

        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());
    }

    @Test
    public void testInsertRetrievalFlag() {

        final StatisticsService statistics = Statistics.getService();

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());

        statistics.setFlag(KEY_FLAG, true);
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).orElseThrow());

        statistics.setFlag(KEY_FLAG, false);
        Assert.assertFalse(statistics.getFlag(KEY_FLAG).orElseThrow());

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());
    }

    @Test
    public void testInsertRetrievalClock() throws InterruptedException {

        final StatisticsService statistics = Statistics.getService();

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());

        statistics.startOrResumeClock(KEY_CLOCK);
        Assert.assertEquals(statistics.getClock(KEY_CLOCK).orElseThrow(), Duration.ZERO);

        // windows has too low of a timer resolution, therefore wait a bit
        Thread.sleep(SLEEP);

        statistics.pauseClock(KEY_CLOCK);
        Duration c1 = statistics.getClock(KEY_CLOCK).orElseThrow();
        Assert.assertTrue(c1.compareTo(Duration.ZERO) > 0);

        statistics.startOrResumeClock(KEY_CLOCK);
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isPresent());

        // windows has too low of a timer resolution, therefore wait a bit
        Thread.sleep(SLEEP);

        statistics.pauseClock(KEY_CLOCK);
        Duration c2 = statistics.getClock(KEY_CLOCK).orElseThrow();
        Assert.assertTrue(c2.compareTo(c1) > 0);

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());
    }

    @Test
    public void testInsertRetrievalCounter() {

        final StatisticsService statistics = Statistics.getService();

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER).isEmpty());

        statistics.increaseCounter(KEY_COUNTER);
        Assert.assertEquals(statistics.getCount(KEY_COUNTER).orElseThrow(), 1);

        statistics.increaseCounter(KEY_COUNTER, 22);
        Assert.assertEquals(statistics.getCount(KEY_COUNTER).orElseThrow(), 23);

        statistics.setCounter(KEY_COUNTER, 42);
        Assert.assertEquals(statistics.getCount(KEY_COUNTER).orElseThrow(), 42);

        Assert.assertTrue(statistics.getText(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isEmpty());
    }

    @Test
    public void testInvalidTimers() {
        final Object owner1 = new Object();
        final Object owner2 = new Object();
        final StatisticsService statistics = Statistics.getService();

        statistics.startOrResumeClock(KEY_CLOCK);
        Assert.assertThrows(IllegalStateException.class, () -> statistics.startOrResumeClock(KEY_CLOCK));

        statistics.startOrResumeClock(KEY_CLOCK, owner1);
        statistics.startOrResumeClock(KEY_CLOCK, owner2);

        Assert.assertThrows(IllegalStateException.class, () -> statistics.startOrResumeClock(KEY_CLOCK, owner1));
        Assert.assertThrows(IllegalStateException.class, () -> statistics.startOrResumeClock(KEY_CLOCK, owner2));

        statistics.pauseClock(KEY_CLOCK);
        Assert.assertTrue(statistics.getClock(KEY_CLOCK).isPresent());
    }

    @Test
    public void testKeyConflict() {

        final Object owner = new Object();
        final StatisticsService statistics = Statistics.getService();

        statistics.setText(KEY_TEXT, "Test");
        statistics.increaseCounter(KEY_COUNTER);

        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.setText(KEY_COUNTER, "Test"));
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.setFlag(KEY_TEXT, true));
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.startOrResumeClock(KEY_TEXT));
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.increaseCounter(KEY_TEXT));

        // Currently, mixed values across different owners are allowed and only result in a logged warning
        statistics.setText(KEY_TEXT, "Test", owner);
        statistics.increaseCounter(KEY_COUNTER, owner);
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.setText(KEY_COUNTER, "Test", owner));
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.setFlag(KEY_TEXT, true, owner));
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.startOrResumeClock(KEY_TEXT, owner));
        Assert.assertThrows(IllegalArgumentException.class, () -> statistics.increaseCounter(KEY_TEXT, owner));
    }

    @Test
    public void testWrongKeyLookups() {

        final Object owner1 = new Object();
        final Object owner2 = new Object();

        final StatisticsService statistics = Statistics.getService();

        statistics.setText(KEY_TEXT, "Test 1");
        statistics.setFlag(KEY_FLAG, true);
        statistics.startOrResumeClock(KEY_CLOCK);
        statistics.pauseClock(KEY_CLOCK);
        statistics.setCounter(KEY_COUNTER, 23);

        Assert.assertTrue(statistics.getTexts(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getText(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getText(KEY_FLAG, owner1).isEmpty());
        Assert.assertTrue(statistics.getText(KEY_TEXT, owner2).isEmpty());

        Assert.assertTrue(statistics.getFlags(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_TEXT, owner1).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG, owner2).isEmpty());

        Assert.assertTrue(statistics.getClocks(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_TEXT, owner1).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK, owner2).isEmpty());

        Assert.assertTrue(statistics.getCounts(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_TEXT, owner1).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER, owner2).isEmpty());

        statistics.setText(KEY_TEXT, "Test 2", owner1);
        statistics.setFlag(KEY_FLAG, false, owner1);
        statistics.startOrResumeClock(KEY_CLOCK, owner1);
        statistics.pauseClock(KEY_CLOCK, owner1);
        statistics.setCounter(KEY_COUNTER, 42, owner1);

        Assert.assertTrue(statistics.getTexts(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getText(KEY_FLAG).isEmpty());
        Assert.assertTrue(statistics.getText(KEY_FLAG, owner1).isEmpty());
        Assert.assertTrue(statistics.getText(KEY_TEXT, owner2).isEmpty());

        Assert.assertTrue(statistics.getFlags(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_TEXT, owner1).isEmpty());
        Assert.assertTrue(statistics.getFlag(KEY_FLAG, owner2).isEmpty());

        Assert.assertTrue(statistics.getClocks(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_TEXT, owner1).isEmpty());
        Assert.assertTrue(statistics.getClock(KEY_CLOCK, owner2).isEmpty());

        Assert.assertTrue(statistics.getCounts(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_TEXT).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_TEXT, owner1).isEmpty());
        Assert.assertTrue(statistics.getCount(KEY_COUNTER, owner2).isEmpty());
    }

    @Test
    public void testDisplay() {

        final Object owner1 = new Object();
        final Object owner2 = new Object();

        final StatisticsService statistics = Statistics.getService();

        statistics.setText(new StatisticsKey(KEY_TEXT.getKey(), "Description"), "Text value");

        statistics.setFlag(KEY_FLAG, true, owner1);
        statistics.setFlag(KEY_FLAG, false, owner2);

        StatisticsKey keyClock = new StatisticsKey(KEY_CLOCK.getKey(), "Duration");
        statistics.startOrResumeClock(keyClock, owner1);
        statistics.startOrResumeClock(keyClock, owner2);
        statistics.pauseClock(keyClock, owner1);
        statistics.pauseClock(keyClock, owner2);

        statistics.setCounter(KEY_COUNTER.withId("id1"), 23, owner1);
        statistics.setCounter(KEY_COUNTER.withId("id2"), 42, owner2);

        Pattern pattern = Pattern.compile("""
                                                  Statistics:
                                                  ============================================
                                                  \\* Description: Text value
                                                  \\* Duration
                                                   {2}\\* Instance \\d+: \\d+ ms
                                                   {2}\\* Instance \\d+: \\d+ ms
                                                  \\* key-counter-id1: 23
                                                  \\* key-counter-id2: 42
                                                  \\* key-flag
                                                   {2}\\* Instance \\d+: (true|false)
                                                   {2}\\* Instance \\d+: (true|false)
                                                  ============================================
                                                  """);

        String output = statistics.print();
        Assert.assertTrue(pattern.matcher(output).matches(), output);
    }
}
