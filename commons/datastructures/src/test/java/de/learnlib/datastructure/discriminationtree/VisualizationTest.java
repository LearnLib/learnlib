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
package de.learnlib.datastructure.discriminationtree;

import java.io.IOException;
import java.io.StringWriter;

import net.automatalib.common.util.IOUtil;
import net.automatalib.serialization.dot.GraphDOT;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VisualizationTest {

    @Test
    public void testVisualization() throws IOException {
        final StringWriter actualDT = new StringWriter();
        GraphDOT.write(DummyDT.DT, actualDT);

        final String expectedDT =
                IOUtil.toString(IOUtil.asBufferedUTF8Reader(VisualizationTest.class.getResourceAsStream("/dt.dot")));

        Assert.assertEquals(actualDT.toString(), expectedDT);
    }
}
