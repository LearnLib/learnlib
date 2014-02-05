package de.learnlib.algorithms.features.observationtable.writer.otsource;

import de.learnlib.algorithms.features.observationtable.InvalidRowException;
import de.learnlib.algorithms.features.observationtable.NoSuchRowException;
import de.learnlib.algorithms.features.observationtable.ObservationTable;
import net.automatalib.words.Word;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SimpleObservationTable implements ObservationTable<String,String> {

	final List<? extends Word<String>> suffixes;

	public SimpleObservationTable(List<? extends Word<String>> suffixes) {
		this.suffixes = suffixes;
	}

	@Nonnull
	@Override
	public Collection<? extends Word<String>> getShortPrefixes() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Collection<? extends Word<String>> getLongPrefixes() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Collection<? extends Word<String>> getAllPrefixes() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public List<? extends Word<String>> getSuffixes() {
		return Collections.unmodifiableList(suffixes);
	}

	@Nonnull
	@Override
	public Word<String> getSuffix(@Nonnegative int index) throws IndexOutOfBoundsException {
		return suffixes.get(index);
	}

	@Nonnull
	@Override
	public Collection<? extends Row<String, String>> getShortPrefixRows() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Collection<? extends Row<String, String>> getLongPrefixRows() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Collection<? extends Row<String, String>> getAllRows() {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Row<String, String> getRow(Word<String> prefix) throws NoSuchRowException {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Row<String, String> getSuccessorRow(Row<String, String> spRow, @Nullable String symbol) throws InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isClosed() {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Row<String, String> findUnclosedRow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConsistent(Collection<? extends String> inputs) {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Inconsistency<String, String> findInconsistency(Collection<? extends String> inputs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int findDistinguishingSuffixIndex(Inconsistency<String, String> inconsistency) throws NoSuchRowException, InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Word<String> findDistinguishingSuffix(Inconsistency<String, String> inconsistency) throws NoSuchRowException, InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int findDistinguishingSuffixIndex(Row<String, String> firstRow, Row<String, String> secondRow) throws InvalidRowException {
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public Word<String> findDistinguishingSuffix(Row<String, String> firstRow, Row<String, String> secondRow) throws InvalidRowException {
		throw new UnsupportedOperationException();
	}
}
