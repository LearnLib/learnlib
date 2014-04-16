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

import de.learnlib.algorithms.features.observationtable.InvalidRowException;
import de.learnlib.algorithms.features.observationtable.NoSuchRowException;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import de.learnlib.algorithms.features.observationtable.OTUtils;
import net.automatalib.words.Word;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the data structure of an {@link ObservationTable} wihout providing
 * any meaningful functionalily. It is used to store the result of reading string representations
 * like with {@link OTUtils#fromString(String, net.automatalib.words.Alphabet, ObservationTableReader)}.
 *
 * @param <I>
 *     The input type.
 * @param <O>
 *     The output type.
 */
public class SimpleObservationTable<I,O> implements ObservationTable<I,O> {

	final List<? extends Word<I>> suffixes;

	public SimpleObservationTable(List<? extends Word<I>> suffixes) {
		this.suffixes = suffixes;
	}

	@Nonnull
	@Override
	public Collection<? extends Word<I>> getShortPrefixes() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Collection<? extends Word<I>> getLongPrefixes() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Collection<? extends Word<I>> getAllPrefixes() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<? extends Word<I>> getSuffixes() {
		return Collections.unmodifiableList(suffixes);
	}

	@Nonnull
	@Override
	public Word<I> getSuffix(@Nonnegative int index) throws IndexOutOfBoundsException {
		return suffixes.get(index);
	}

	@Nonnull
	@Override
	public Collection<? extends Row<I, O>> getShortPrefixRows() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Collection<? extends Row<I, O>> getLongPrefixRows() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Collection<? extends Row<I, O>> getAllRows() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public Row<I, O> getRow(Word<I> prefix) throws NoSuchRowException {
		throw new NoSuchRowException();
	}

	@Nullable
	@Override
	public Row<I, O> getSuccessorRow(Row<I, O> spRow, @Nullable I symbol) throws InvalidRowException {
		throw new InvalidRowException();
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Nullable
	@Override
	public Row<I, O> findUnclosedRow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConsistent(Collection<? extends I> inputs) {
		return false;
	}

	@Nullable
	@Override
	public Inconsistency<I, O> findInconsistency(Collection<? extends I> inputs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int findDistinguishingSuffixIndex(Inconsistency<I, O> inconsistency) throws NoSuchRowException, InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Word<I> findDistinguishingSuffix(Inconsistency<I, O> inconsistency) throws NoSuchRowException, InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int findDistinguishingSuffixIndex(Row<I, O> firstRow, Row<I, O> secondRow) throws InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Word<I> findDistinguishingSuffix(Row<I, O> firstRow, Row<I, O> secondRow) throws InvalidRowException {
		throw new UnsupportedOperationException();
	}
}
