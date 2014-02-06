package de.learnlib.algorithms.features.observationtable.writer;

import de.learnlib.algorithms.features.observationtable.OTUtils;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.reader.SuffixASCIIReader;
import de.learnlib.algorithms.features.observationtable.writer.otsource.ObservationTableSource;
import junit.framework.Assert;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;
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
