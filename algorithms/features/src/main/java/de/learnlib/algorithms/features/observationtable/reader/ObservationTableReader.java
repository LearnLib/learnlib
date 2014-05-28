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
package de.learnlib.algorithms.features.observationtable.reader;

import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Alphabet;

import javax.annotation.Nonnull;

/**
 * Reads an {@link ObservationTable} from a string source.
 *
 * @param <I>
 * @param <D>
 */
public interface ObservationTableReader<I,D> {

	/**
	 * Reads the string representation of an observation table and returns an implementation
	 * of {@link ObservationTable} which allows access to short and long prefixes as well as
	 * suffixes.
	 *
	 * @param source
	 *      The string representation to read, must not be {@code null}.
	 * @param alphabet
	 *      The learning alphabet containing the symbols used, must not be {@code null}.
	 *
	 * @return
	 *      an implementation of {@link ObservationTable} which allows at lease access to
	 *      {@link ObservationTable#getSuffixes()}, {@link ObservationTable#getShortPrefixes()},
	 *      and {@link ObservationTable#getLongPrefixes()}. Will never be {@code null}.
	 */
	@Nonnull
	public ObservationTable<I,D> read(@Nonnull String source, @Nonnull Alphabet<I> alphabet);

}
