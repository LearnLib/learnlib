package de.learnlib.algorithms.angluin;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ObservationTableRowTest {

	@Test
	public void testEquals() throws Exception {
		ObservationTableRow first = new ObservationTableRow();

		Assert.assertEquals(first, first);
		Assert.assertNotEquals(first.getValues(), first);

		ObservationTableRow second = new ObservationTableRow();

		Assert.assertEquals(first, second);


		first.getValues().add(true);

		Assert.assertNotEquals(first, second);

		second.getValues().add(false);

		Assert.assertNotEquals(first, second);

		second.getValues().clear();
		second.getValues().add(true);

		Assert.assertEquals(first, second);
	}
}
