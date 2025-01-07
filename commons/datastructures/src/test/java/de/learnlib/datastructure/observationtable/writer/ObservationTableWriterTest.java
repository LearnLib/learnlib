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
package de.learnlib.datastructure.observationtable.writer;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.ObservationTableSource;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ObservationTableWriterTest {

    @Test
    public void testWriteASCII() throws URISyntaxException, IOException {

        final ObservationTableASCIIWriter<String, String> writer =
                new ObservationTableASCIIWriter<>(input -> String.join(" ", input), output -> "out: " + output, true);

        testInternal(writer, "/OT_ASCII.txt");
    }

    @Test
    public void testWriteHTML() throws URISyntaxException, IOException {

        final ObservationTableHTMLWriter<String, String> writer =
                new ObservationTableHTMLWriter<>(input -> String.join(" ", input), output -> "out: " + output);

        testInternal(writer, "/OT_HTML.html");
    }

    private static void testInternal(AbstractObservationTableWriter<String, String> writer, String urlOfExpectedResult)
            throws URISyntaxException, IOException {

        final ObservationTable<String, String> ot = ObservationTableSource.otWithFourSuffixes();

        final StringWriter writerResult = new StringWriter();
        writer.write(ot, writerResult);

        final URL resource = ObservationTableWriterTest.class.getResource(urlOfExpectedResult);
        Assert.assertNotNull(resource);
        final String expectedResult =
                new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);

        Assert.assertEquals(writerResult.toString(), expectedResult);
    }

}
