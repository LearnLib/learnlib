package de.learnlib.algorithms.features.observationtable.writer;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;

import de.learnlib.algorithms.features.observationtable.ObservationTable;

@ParametersAreNonnullByDefault
public interface ObservationTableWriter<I, O> {
	public void write(ObservationTable<? extends I,? extends O> table, @WillNotClose Appendable out) throws IOException;
	public void write(ObservationTable<? extends I,? extends O> table, @WillNotClose PrintStream out);
	public void write(ObservationTable<? extends I,? extends O> table, @WillNotClose StringBuilder out);
	public void write(ObservationTable<? extends I,? extends O> table, File file) throws IOException;
}
