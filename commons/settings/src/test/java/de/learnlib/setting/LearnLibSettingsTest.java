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
package de.learnlib.setting;

import net.automatalib.common.setting.AutomataLibProperty;
import net.automatalib.common.setting.AutomataLibSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LearnLibSettingsTest {

    @Test
    public void testLearnLibProperties() {
        LearnLibSettings settings = LearnLibSettings.getInstance();

        for (LearnLibProperty p : LearnLibProperty.values()) {
            switch (p) {
                case PARALLEL_BATCH_SIZE_DYNAMIC:
                    Assert.assertEquals(settings.getInt(LearnLibProperty.PARALLEL_BATCH_SIZE_DYNAMIC, 0), 1);
                    break;
                case PARALLEL_BATCH_SIZE_STATIC:
                    Assert.assertEquals(settings.getInt(LearnLibProperty.PARALLEL_BATCH_SIZE_STATIC, 0), 2);
                    break;
                case PARALLEL_POOL_POLICY:
                    Assert.assertEquals(settings.getProperty(LearnLibProperty.PARALLEL_POOL_POLICY), "CACHED");
                    break;
                case PARALLEL_POOL_SIZE:
                    Assert.assertEquals(settings.getInt(LearnLibProperty.PARALLEL_POOL_SIZE, 0), 3);
                    break;
                default:
                    throw new IllegalStateException("Unhandled property " + p);
            }
        }
    }

    @Test
    public void testAutomataLibProperties() {
        AutomataLibSettings settings = AutomataLibSettings.getInstance();

        // LearnLib should load properties for AutomataLib from learnlib.properties files but prefer automatalib.properties
        Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_EMPTY_REP), "empty_rep");
        Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_DELIM_RIGHT), "delim_right");
        Assert.assertEquals(settings.getProperty(AutomataLibProperty.WORD_DELIM_LEFT), "delim_left_override");
    }
}
