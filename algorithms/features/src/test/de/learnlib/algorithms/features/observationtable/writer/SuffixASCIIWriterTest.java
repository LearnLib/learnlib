package de.learnlib.algorithms.features.observationtable.writer;

import de.learnlib.algorithms.features.observationtable.OTUtils;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.writer.otsource.ObservationTableSource;
import junit.framework.Assert;
import org.testng.annotations.Test;

public class SuffixASCIIWriterTest {

	@Test
	public void testWrite() throws Exception {
		SuffixASCIIWriter<String,String> writer = new SuffixASCIIWriter<>();
		ObservationTable<String,String> ot = ObservationTableSource.otWithFourSuffixes();
		Assert.assertEquals(OTUtils.toString(ot, writer), ";A;B;A,B");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testDelimiterInNames() throws Exception {
		SuffixASCIIWriter<String,String> writer = new SuffixASCIIWriter<>();
		ObservationTable<String,String> ot = ObservationTableSource.otWithFourSuffixesUsingDelimiterInNames();

		//noinspection ResultOfMethodCallIgnored
		OTUtils.toString(ot, writer);
	}
}
