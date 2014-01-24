/* Copyright (C) 2013 TU Dortmund
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
package de.learnlib.algorithms.baselinelstar;

import com.google.common.collect.Lists;
import de.learnlib.algorithms.features.observationtable.AbstractObservationTable;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The internal storage mechanism for {@link BaselineLStar}.
 *
 * @param <I>
 * 		input symbol class.
 * @param <O>
 * 		output symbol class.
 */
public class ObservationTable<I, O> extends AbstractObservationTable<I, O> {

	@Nonnull
	private final List<ObservationTableRow<I, O>> shortPrefixRows; // S

	@Nonnull
	private final List<ObservationTableRow<I, O>> longPrefixRows;  // SA

	@Nonnull
	private final List<Word<I>> suffixes;                          // E


	public ObservationTable() {
		Word<I> emptyWord = Word.epsilon();

		suffixes = new ArrayList<>();
		suffixes.add(emptyWord);

		Word<I> epsiplon = Word.epsilon();
		ObservationTableRow<I, O> initialRow = new ObservationTableRow<>(epsiplon);
		initialRow.setShortPrefixRow();
		shortPrefixRows = new LinkedList<>();
		shortPrefixRows.add(initialRow);

		longPrefixRows = new LinkedList<>();
	}

	/**
	 * The set of suffixes in the observation table, often called "E".
	 *
	 * @return The set of candidates.
	 */
	@Override
	public List<Word<I>> getSuffixes() {
		return Collections.unmodifiableList(suffixes);
	}

	void addSuffix(Word<I> suffix) {
		suffixes.add(suffix);
	}

	@Override
	public Collection<ObservationTableRow<I, O>> getShortPrefixRows() {
		return Collections.unmodifiableCollection(shortPrefixRows);
	}

	@Nonnull
	public List<Word<I>> getShortPrefixLabels() {
		List<Word<I>> labels = Lists.newArrayListWithExpectedSize(shortPrefixRows.size());
		for (ObservationTableRow<I, O> row : shortPrefixRows) {
			labels.add(row.getLabel());
		}
		return labels;
	}

	@Override
	public Collection<ObservationTableRow<I, O>> getLongPrefixRows() {
		return Collections.unmodifiableCollection(longPrefixRows);
	}

	@Nonnull
	public List<Word<I>> getLongPrefixLabels() {
		List<Word<I>> labels = Lists.newArrayListWithExpectedSize(longPrefixRows.size());
		for (ObservationTableRow<I, O> row : longPrefixRows) {
			labels.add(row.getLabel());
		}
		return labels;
	}

	@Override
	@Nullable
	public Row<I, O> getSuccessorRow(@Nonnull Row<I, O> spRow, I symbol) {
		//noinspection SuspiciousMethodCalls
		if (!shortPrefixRows.contains(spRow)) {
			throw new IllegalArgumentException("Row '" + spRow + "' is not part of short prefix rows!");
		}

		Word<I> successorLabel = spRow.getLabel().append(symbol);

		Row<I, O> successor = null;

		for (Row<I, O> row : getAllRows()) {
			if (row.getLabel().equals(successorLabel)) {
				successor = row;
				break;
			}
		}

		return successor;
	}

	void addShortPrefix(@Nonnull Word<I> shortPrefix) {
		final ObservationTableRow<I, O> row = new ObservationTableRow<>(shortPrefix);
		row.setShortPrefixRow();
		shortPrefixRows.add(row);
	}

	void addLongPrefix(@Nonnull Word<I> longPrefix) {
		final ObservationTableRow<I, O> row = new ObservationTableRow<>(longPrefix);
		row.setLongPrefixRow();
		longPrefixRows.add(row);
	}

	void removeShortPrefixesFromLongPrefixes() {
		List<Word<I>> longPrefixLabels = getLongPrefixLabels();
		longPrefixLabels.retainAll(getShortPrefixLabels());

		List<ObservationTableRow<I, O>> rowsToRemove = Lists.newArrayListWithCapacity(longPrefixLabels.size());

		for (ObservationTableRow<I, O> row : longPrefixRows) {
			if (longPrefixLabels.contains(row.getLabel())) {
				rowsToRemove.add(row);
			}
		}

		longPrefixRows.removeAll(rowsToRemove);
	}

	/**
	 * Adds the result of a membership query to this table.
	 *
	 * @param prefix
	 * 		The prefix of the {@link Word} asked with the membership query.
	 * @param suffix
	 * 		The prefix of the {@link Word} asked with the membership query.
	 * @param result
	 * 		The result of the query.
	 */
	void addResult(@Nonnull Word<I> prefix, @Nonnull Word<I> suffix, @Nonnull O result) {
		if (!suffixes.contains(suffix)) {
			throw new IllegalArgumentException("Suffix '" + suffix + "' is not part of the suffixes set");
		}

		final int suffixPosition = suffixes.indexOf(suffix);
		ObservationTableRow<I, O> row = getRowForPrefix(prefix);

		addResultToRow(result, suffixPosition, row);
	}

	private void addResultToRow(@Nonnull O result, int suffixPosition, @Nonnull ObservationTableRow<I, O> row) {
		final List<O> values = row.getValues();
		if (values.size() > suffixPosition) {
			if (!values.get(suffixPosition).equals(result)) {
				throw new IllegalStateException(
						"New result " + values.get(suffixPosition) + " differs from old result " + result);
			}
		}
		else {
			row.addValue(result);
		}
	}

	/**
	 * Determines the next state for which the observation table needs to be closed.
	 *
	 * @return The next state for which the observation table needs to be closed. If the
	 * table is closed, this returns {@code null}.
	 */
	@Nullable
	Word<I> findUnclosedState() {
		for (ObservationTableRow<I, O> candidate : longPrefixRows) {
			boolean found = false;

			for (ObservationTableRow<I, O> stateRow : shortPrefixRows) {
				if (candidate.getValues().equals(stateRow.getValues())) {
					found = true;
					break;
				}
			}

			if (!found) {
				return candidate.getLabel();
			}
		}

		return null;
	}

	/**
	 * @param alphabet
	 * 		The {@link Alphabet} for which the consistency is checked
	 * @return if the observation table is consistent with the given alphabet.
	 */
	boolean isConsistentWithAlphabet(@Nonnull Alphabet<I> alphabet) {
		return findInconsistentSymbol(alphabet) == null;
	}

	@Nullable
	InconsistencyDataHolder<I> findInconsistentSymbol(@Nonnull Alphabet<I> alphabet) {
		for (I symbol : alphabet) {
			for (int firstStateCounter = 0; firstStateCounter < shortPrefixRows.size(); firstStateCounter++) {
				Word<I> firstState = shortPrefixRows.get(firstStateCounter).getLabel();

				for (int secondStateCounter = firstStateCounter + 1; secondStateCounter < shortPrefixRows.size();
				     secondStateCounter++) {
					Word<I> secondState = shortPrefixRows.get(secondStateCounter).getLabel();

					if (checkInconsistency(firstState, secondState, symbol)) {
						return new InconsistencyDataHolder<>(firstState, secondState, symbol);
					}
				}
			}
		}

		return null;
	}

	private boolean checkInconsistency(@Nonnull Word<I> firstState, @Nonnull Word<I> secondState,
			@Nonnull I alphabetSymbol) {
		ObservationTableRow<I, O> rowForFirstState = getRowForPrefix(firstState);
		ObservationTableRow<I, O> rowForSecondState = getRowForPrefix(secondState);

		if (!rowForFirstState.getValues().equals(rowForSecondState.getValues())) {
			return false;
		}

		Word<I> extendedFirstState = firstState.append(alphabetSymbol);
		Word<I> extendedSecondState = secondState.append(alphabetSymbol);
		ObservationTableRow rowForExtendedFirstState = getRowForPrefix(extendedFirstState);
		ObservationTableRow rowForExtendedSecondState = getRowForPrefix(extendedSecondState);

		return !rowForExtendedFirstState.getValues().equals(rowForExtendedSecondState.getValues());
	}

	@Nonnull
	Word<I> determineWitnessForInconsistency(@Nonnull InconsistencyDataHolder<I> dataHolder) {
		Word<I> firstState = dataHolder.getFirstState().append(dataHolder.getDifferingSymbol());
		Word<I> secondState = dataHolder.getSecondState().append(dataHolder.getDifferingSymbol());

		ObservationTableRow<I, O> firstRow = getRowForPrefix(firstState);
		ObservationTableRow<I, O> secondRow = getRowForPrefix(secondState);

		for (int i = 0; i < firstRow.getValues().size(); i++) {
			O symbolFirstRow = firstRow.getValues().get(i);
			O symbolSecondRow = secondRow.getValues().get(i);
			if (!symbolFirstRow.equals(symbolSecondRow)) {
				return suffixes.get(i);
			}
		}

		throw new IllegalStateException("Both rows are identical, unable to determine a witness!");
	}

	@Nonnull
	ObservationTableRow<I, O> getRowForPrefix(@Nonnull Word<I> state) {
		for (ObservationTableRow<I, O> row : shortPrefixRows) {
			if (row.getLabel().equals(state)) {
				return row;
			}
		}

		for (ObservationTableRow<I, O> row : longPrefixRows) {
			if (row.getLabel().equals(state)) {
				return row;
			}
		}

		throw new IllegalArgumentException("Unable to find a row for '" + state + "'");
	}

	/**
	 * Moves a single row from long prefix rows to short prefix rows.
	 *
	 * @param longPrefix
	 * 		A row which must be part of the long prefix rows and
	 * 		should be moved to the short prefix rows.
	 */
	void moveLongPrefixToShortPrefixes(@Nonnull Word<I> longPrefix) {
		ObservationTableRow<I, O> rowToMove = null;

		for (ObservationTableRow<I, O> row : longPrefixRows) {
			if (row.getLabel().equals(longPrefix)) {
				rowToMove = row;
				break;
			}
		}

		if (rowToMove == null) {
			throw new IllegalArgumentException("Word '" + longPrefix + "' not part of long prefixes");
		}

		longPrefixRows.remove(rowToMove);
		rowToMove.setShortPrefixRow();
		shortPrefixRows.add(rowToMove);
	}
}
