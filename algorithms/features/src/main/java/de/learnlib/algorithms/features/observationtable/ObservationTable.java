/* Copyright (C) 2014 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.features.observationtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Signed;

import net.automatalib.words.Word;

import com.google.common.base.Objects;

/**
 * An observation table is a common method for learning algorithms to store organize
 * their observations. This interface defines a generic API for interacting with
 * homogeneously interacting with observation tables regardless of their implementation.
 * <p>
 * Instances implementing this interface can be obtained from learning algorithms implementing
 * the {@link ObservationTableFeature observation table feature} (or {@link OTLearner}s.
 * <p>
 * Basically, an observation table is a two-dimensional table, where both rows and columns
 * are indexed by {@link Word}s. The row indices are called <i>prefixes</i>, whereas
 * the column indexes are referred to as <i>suffixes</i>. The table is further vertically
 * divided into two halves: the prefixes in the upper half are referred to as
 * <i>short prefixes</i> (these usually correspond to states in learned hypothesis automata),
 * whereas the prefixes in the lower half are referred to as <i>long prefixes</i>. Long
 * prefixes must be one-letter extensions of short prefixes; they refer to transitions
 * in the hypothesis automaton. We refer to rows as <i>short prefix rows</i> or <i>long prefix
 * row</i>, depending on whether they occur in the upper or lower half of the table respectively. 
 * <p>
 * The cells of the table are filled with observations for a given prefix and suffix combination.
 * The type of observations is generic and can be specified using the type parameter {@code O}.
 * <p>
 * There are two important properties of observation tables, which usually have to be satisfied
 * in order to be able to generate an automaton from an observation table: it must be both
 * <i>closed</i> and <i>consistent</i>.
 * <p>
 * In a <b>closed</b> observation table, the contents of <i>each</i> long prefix row equal the
 * contents of at least one short prefix rows. <b>Consistency</b>, on the other hand, is satisfied
 * when for every two distinct short prefix rows, all rows indexed by one-letter extensions
 * of the corresponding prefix with any input symbol also have the same content.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> observation (output) domain type
 */
@ParametersAreNonnullByDefault
public interface ObservationTable<I, D> {
	
	/**
	 * Used to indicate that no distinguishing suffix exists in
	 * {@link #findDistinguishingSuffixIndex(Inconsistency)} and
	 * {@link #findDistinguishingSuffixIndex(Row, Row)}.
	 */
	public static final int NO_DISTINGUISHING_SUFFIX = -1;
	
	/**
	 * A single row in the observation table.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type 
	 * @param <D> output domain type
	 */
	public static interface Row<I,D> extends Iterable<D> {
		/**
		 * Retrieves the label of this row.
		 * @return the label of this row
		 */
		@Nonnull
		public Word<I> getLabel();
		
		/**
		 * Retrieves whether this row is a short or a long prefix row.
		 * @return {@code true} if this row is a short prefix row, {@code false}
		 * otherwise.
		 */
		public boolean isShortPrefixRow();
		
		/**
		 * Retrieves a list of the cell contents in this row.
		 * @return the cell contents in this row
		 */
		@Nonnull
		public List<? extends D> getContents();
		
		/**
		 * Retrieves the size (length) of this row.
		 * @return the size of this row
		 */
		public int size();
		
		/**
		 * Retrieves the cell content at the given index.
		 * @param index the index
		 * @return the cell content at the given index
		 * @throws IndexOutOfBoundsException if the given index is invalid, i.e., is less than
		 * {@code 0} or greater than or equal to {@code size()}
		 */
		@Nullable
		public D getCellContent(@Nonnegative int index) throws IndexOutOfBoundsException;
	}
	
	/**
	 * Representation of an inconsistency in the observation table.
	 * <p>
	 * An inconsistency is represented by two short prefix rows with
	 * matching contents, and an input symbol such that the rows indexed
	 * by one-letter extensions have differing contents.
	 * 
	 * @author Malte Isberner
	 *
	 * @param <I> input symbol type
	 * @param <D> observation type
	 */
	public static interface Inconsistency<I,D> {
		/**
		 * Retrieves the first (short prefix) row constituting the inconsistency.
		 * @return the first row
		 */
		@Nonnull
		public Row<I,D> getFirstRow();
		
		/**
		 * Retrieves the second (short prefix) row constituting the inconsistency
		 * @return the second row
		 */
		@Nonnull
		public Row<I,D> getSecondRow();
		
		/**
		 * Retrieves the symbol for which's one-letter extensions the corresponding
		 * rows have different contents. 
		 * @return the symbol
		 */
		@Nullable
		public I getSymbol();
	}
	
	public static abstract class AbstractRow<I,D> implements Row<I,D> {

		@Override
		@SuppressWarnings("unchecked")
		public Iterator<D> iterator() {
			return (Iterator<D>)Collections.unmodifiableCollection(getContents()).iterator();
		}

		@Override
		public int size() {
			return getContents().size();
		}
		
		@Override
		public D getCellContent(int index) {
			return getContents().get(index);
		}
		
	}
	
	public static class DefaultInconsistency<I,D> implements Inconsistency<I,D> {
		private final Row<I,D> firstRow;
		private final Row<I,D> secondRow;
		private final I symbol;
		
		public DefaultInconsistency(Row<I,D> firstRow, Row<I,D> secondRow, I symbol) {
			this.firstRow = firstRow;
			this.secondRow = secondRow;
			this.symbol = symbol;
		}
		
		@Override
		public Row<I,D> getFirstRow() {
			return firstRow;
		}
		
		@Override
		public Row<I,D> getSecondRow() {
			return secondRow;
		}
		
		@Override
		public I getSymbol() {
			return symbol;
		}
	}
	
	/**
	 * Retrieves the short prefixes in the table. The prefixes are returned
	 * in no specified order.
	 * @return the short prefixes in the table
	 */
	@Nonnull
	default public Collection<? extends Word<I>> getShortPrefixes() {
		Collection<? extends Row<I,D>> spRows = getShortPrefixRows();
		return spRows.stream().map(Row::getLabel).collect(Collectors.toList());
		
	}
	
	/**
	 * Retrieves the long prefixes in the table. The prefixes are returned
	 * in no specified order.
	 * @return the long prefixes in the table
	 */
	@Nonnull
	default public Collection<? extends Word<I>> getLongPrefixes() {
		Collection<? extends Row<I,D>> lpRows = getLongPrefixRows();
		return lpRows.stream().map(Row::getLabel).collect(Collectors.toList());
	}
	
	/**
	 * Retrieves all prefixes (short and long) in the table. The prefixes are
	 * returned in no specified order.
	 * @return all prefixes in the table
	 */
	@Nonnull
	default public Collection<? extends Word<I>> getAllPrefixes() {
		Collection<? extends Word<I>> shortPrefixes = getShortPrefixes();
		Collection<? extends Word<I>> longPrefixes = getLongPrefixes();
		List<Word<I>> result = new ArrayList<>(shortPrefixes.size() + longPrefixes.size());
		
		result.addAll(shortPrefixes);
		result.addAll(longPrefixes);
		
		return result;
	}

	@Nonnull
	default public Collection<? extends Row<I, D>> getAllRows() {
		Collection<? extends Row<I,D>> spRows = getShortPrefixRows();
		Collection<? extends Row<I,D>> lpRows = getLongPrefixRows();
		
		List<Row<I,D>> result = new ArrayList<>(spRows.size() + lpRows.size());
		result.addAll(spRows);
		result.addAll(lpRows);
		
		return result;
	}

	@Nullable
	default public Row<I, D> getRow(Word<I> prefix) {
		for(Row<I,D> row : getAllRows()) {
			if(prefix.equals(row.getLabel())) {
				return row;
			}
		}
		
		return null;
	}

	default public boolean isClosed() {
		return (findUnclosedRow() == null);
	}

	@Nullable
	default public Row<I, D> findUnclosedRow() {
		Set<List<? extends D>> spRowContents = new HashSet<>();
		for(Row<I,D> spRow : getShortPrefixRows()) {
			spRowContents.add(spRow.getContents());
		}
		
		for(Row<I,D> lpRow : getLongPrefixRows()) {
			if(!spRowContents.contains(lpRow.getContents())) {
				return lpRow;
			}
		}
		
		return null;
	}

	@Nullable
	default public Inconsistency<I, D> findInconsistency(
			Collection<? extends I> inputs) {
		Map<List<? extends D>,Row<I,D>> spRowsByContent = new HashMap<>();
		for(Row<I,D> spRow : getShortPrefixRows()) {
			List<? extends D> content = spRow.getContents();
			Row<I,D> canonicalRow = spRowsByContent.get(content);
			if(canonicalRow != null) {
				for(I inputSym : inputs) {
					Row<I,D> spRowSucc = getSuccessorRow(spRow, inputSym);
					Row<I,D> canRowSucc = getSuccessorRow(canonicalRow, inputSym);
					if(spRowSucc != canRowSucc) {
						if(!spRowSucc.getContents().equals(canRowSucc.getContents())) {
							return new DefaultInconsistency<>(spRow, canonicalRow, inputSym);
						}
					}
				}
			}
			else {
				spRowsByContent.put(content, spRow);
			}
		}
		
		return null;
	}
	
	/**
	 * Retrieves a suffix by its (column) index.
	 * 
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	@Nonnull
	default public Word<I> getSuffix(int index) {
		return getSuffixes().get(index);
	}
	
	/**
	 * 
	 * @param inconsistency
	 * @return the suffix (column) index where the contents of the rows differ, or
	 * {@code #NO_DISTINGUISHING_SUFFIX} if the contents of the rows are equal.
	 * @throws NoSuchRowException if the 
	 */
	@Signed
	default public int findDistinguishingSuffixIndex(Inconsistency<I, D> inconsistency) {
		Row<I,D> row1 = inconsistency.getFirstRow();
		Row<I,D> row2 = inconsistency.getSecondRow();
		I sym = inconsistency.getSymbol();
		
		Row<I,D> succRow1 = getSuccessorRow(row1, sym);
		Row<I,D> succRow2 = getSuccessorRow(row2, sym);
		
		return findDistinguishingSuffixIndex(succRow1, succRow2);
	}
	
	@Nullable
	default public Word<I> findDistinguishingSuffix(Inconsistency<I, D> inconsistency) {
		int suffixIndex = findDistinguishingSuffixIndex(inconsistency);
		if(suffixIndex != NO_DISTINGUISHING_SUFFIX) {
			return null;
		}
		return getSuffix(suffixIndex);
	}

	/**
	 * 
	 * @param row1 the first row
	 * @param row2 the second row
	 * @return the suffix distinguishing the contents of the two rows
	 * @throws InvalidRowException if the rows do not belong to this observation table
	 */
	@Nullable
	default public Word<I> findDistinguishingSuffix(Row<I, D> row1, Row<I, D> row2) {
		int suffixIndex = findDistinguishingSuffixIndex(row1, row2);
		if(suffixIndex != NO_DISTINGUISHING_SUFFIX) {
			return null;
		}
		return getSuffix(suffixIndex);
	}
	
	/**
	 * 
	 * @param row1 the first row
	 * @param row2 the second row
	 * @return the suffix (column) index where the contents of the rows differ, or
	 * {@code #NO_DISTINGUISHING_SUFFIX} if the contents of the rows are equal.  
	 * @throws InvalidRowException if the rows do not belong to this observation table
	 */
	@Signed
	default public int findDistinguishingSuffixIndex(Row<I,D> row1, Row<I,D> row2) {
		Iterator<? extends D> values1It = row1.getContents().iterator();
		Iterator<? extends D> values2It = row2.getContents().iterator();
		
		int i = 0;
		while(values1It.hasNext() && values2It.hasNext()) {
			D value1 = values1It.next();
			D value2 = values2It.next();
			
			if(!Objects.equal(value1, value2)) {
				return i;
			}
			i++;
		}
		
		if(values1It.hasNext() || values2It.hasNext()) {
			throw new IllegalStateException("Rows [" + row1.getLabel() + "] and/or [" + row2.getLabel() + "] have invalid length");
		}
		
		return NO_DISTINGUISHING_SUFFIX;
	}
	
	default public boolean isConsistent(Collection<? extends I> inputs) {
		return (findInconsistency(inputs) == null);
	}

	/**
	 * Retrieves all suffixes in the table.
	 * @return all suffixes in the table
	 */
	@Nonnull
	public List<? extends Word<I>> getSuffixes();
	
	@Nonnull
	public Collection<? extends Row<I,D>> getShortPrefixRows();
	@Nonnull
	public Collection<? extends Row<I,D>> getLongPrefixRows();
	
	@Nullable
	public Row<I,D> getSuccessorRow(Row<I,D> spRow, @Nullable I symbol) throws InvalidRowException;
		
}
