/* Copyright (C) 2013-2018 TU Dortmund
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
package de.learnlib.datastructures.writer;

import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.reader.SuffixASCIIReader;
import de.learnlib.datastructure.observationtable.writer.SuffixASCIIWriter;
import de.learnlib.datastructures.writer.otsource.ObservationTableSource;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;
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

        //noinspection ResultOfMethodCallIgnored
        OTUtils.toString(ot, writer);
    }

    @Test
    public void testRead() {
        ObservationTable<String, String> ot = ObservationTableSource.otWithFourSuffixes();
        String str = OTUtils.toString(ot, new SuffixASCIIWriter<>());

        Alphabet<String> alphabet = new SimpleAlphabet<>();
        alphabet.add("A");
        alphabet.add("B");

        ObservationTable<String, String> parsedOt =
                OTUtils.fromString(str, alphabet, new SuffixASCIIReader<String, String>());

        Assert.assertEquals(ot.getSuffixes(), parsedOt.getSuffixes());
    }
}
