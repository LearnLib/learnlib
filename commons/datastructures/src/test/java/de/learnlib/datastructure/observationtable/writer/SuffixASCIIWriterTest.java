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

import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.ObservationTableSource;
import de.learnlib.datastructure.observationtable.reader.SuffixASCIIReader;
import net.automatalib.alphabet.GrowingAlphabet;
import net.automatalib.alphabet.impl.GrowingMapAlphabet;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SuffixASCIIWriterTest {

    @Test
    public void testWrite() {
        SuffixASCIIWriter<String, String> writer = new SuffixASCIIWriter<>();
        ObservationTable<String, String> ot = ObservationTableSource.otWithFourSuffixes();
        Assert.assertEquals(OTUtils.toString(ot, writer), ";A;B;A,B");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testDelimiterInNames() {
        SuffixASCIIWriter<String, String> writer = new SuffixASCIIWriter<>();
        ObservationTable<String, String> ot = ObservationTableSource.otWithFourSuffixesUsingDelimiterInNames();

        OTUtils.toString(ot, writer);
    }

    @Test
    public void testRead() {
        ObservationTable<String, String> ot = ObservationTableSource.otWithFourSuffixes();
        String str = OTUtils.toString(ot, new SuffixASCIIWriter<>());

        final GrowingAlphabet<String> alphabet = new GrowingMapAlphabet<>();
        alphabet.add("A");
        alphabet.add("B");

        ObservationTable<String, String> parsedOt = OTUtils.fromString(str, alphabet, new SuffixASCIIReader<>());

        Assert.assertEquals(ot.getSuffixes(), parsedOt.getSuffixes());
    }
}
