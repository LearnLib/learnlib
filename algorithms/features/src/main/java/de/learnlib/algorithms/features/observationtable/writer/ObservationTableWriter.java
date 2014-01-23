package de.learnlib.algorithms.features.observationtable.writer;

import java.io.IOException;

import de.learnlib.algorithms.features.observationtable.ObservationTable;

public interface ObservationTableWriter<I, O> {
	public void write(Appendable out, ObservationTable<? extends I,? extends O> table) throws IOException;
}
