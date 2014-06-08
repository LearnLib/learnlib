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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.WillNotClose;

import de.learnlib.algorithms.features.observationtable.ObservationTable;

@ParametersAreNonnullByDefault
public interface ObservationTableWriter<I, D> {
	public void write(ObservationTable<? extends I,? extends D> table, @WillNotClose Appendable out) throws IOException;
	public void write(ObservationTable<? extends I,? extends D> table, @WillNotClose PrintStream out);
	public void write(ObservationTable<? extends I,? extends D> table, @WillNotClose StringBuilder out);
	public void write(ObservationTable<? extends I,? extends D> table, File file) throws IOException;
}
