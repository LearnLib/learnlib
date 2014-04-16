/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 *
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.algorithms.features.observationtable.writer;

import de.learnlib.algorithms.features.observationtable.OTUtils;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.reader.SuffixASCIIReader;
import de.learnlib.algorithms.features.observationtable.writer.otsource.ObservationTableSource;

import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SuffixASCIIWriterTest {

	@Test
	public void testWrite() {
		SuffixASCIIWriter<String,String> writer = new SuffixASCIIWriter<>();
		ObservationTable<String,String> ot = ObservationTableSource.otWithFourSuffixes();
		Assert.assertEquals(OTUtils.toString(ot, writer), ";A;B;A,B");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testDelimiterInNames() {
		SuffixASCIIWriter<String,String> writer = new SuffixASCIIWriter<>();
		ObservationTable<String,String> ot = ObservationTableSource.otWithFourSuffixesUsingDelimiterInNames();

		//noinspection ResultOfMethodCallIgnored
		OTUtils.toString(ot, writer);
	}

	@Test
	public void testRead() {
		ObservationTable<String,String> ot = ObservationTableSource.otWithFourSuffixes();
		String str = OTUtils.toString(ot, new SuffixASCIIWriter<String, String>());

		Alphabet<String> alphabet = new SimpleAlphabet<>();
		alphabet.add("A");
		alphabet.add("B");

		ObservationTable<String,String> parsedOt =
				OTUtils.fromString(str, alphabet, new SuffixASCIIReader<String,String>());

		Assert.assertEquals(ot.getSuffixes(), parsedOt.getSuffixes());
	}
}
