/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.setting;

import java.io.File;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class LearnLibSettingsTest {

    @BeforeSuite
    public void setUp() {
        final URL resource = LearnLibSettingsTest.class.getResource("/learnlib.properties");
        assert resource != null;
        final File properties = new File(resource.getFile());
        System.setProperty("learnlib.properties", properties.getAbsolutePath());
    }

    @Test
    public void testProperties() {
        LearnLibSettings settings = LearnLibSettings.getInstance();

        for (LearnLibProperty p : LearnLibProperty.values()) {
            switch (p) {
                case PARALLEL_BATCH_SIZE_DYNAMIC:
                    Assert.assertEquals(1, settings.getInt(LearnLibProperty.PARALLEL_BATCH_SIZE_DYNAMIC, 0));
                    break;
                case PARALLEL_BATCH_SIZE_STATIC:
                    Assert.assertEquals(2, settings.getInt(LearnLibProperty.PARALLEL_BATCH_SIZE_STATIC, 0));
                    break;
                case PARALLEL_POOL_POLICY:
                    Assert.assertEquals("CACHED", settings.getProperty(LearnLibProperty.PARALLEL_POOL_POLICY));
                    break;
                case PARALLEL_POOL_SIZE:
                    Assert.assertEquals(3, settings.getInt(LearnLibProperty.PARALLEL_POOL_SIZE, 0));
                    break;
                default:
                    throw new IllegalStateException("Unhandled property " + p);
            }
        }
    }
}
