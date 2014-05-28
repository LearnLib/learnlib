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
package de.learnlib.algorithms.features.observationtable;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Signed;

import net.automatalib.words.Word;

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
	
	/**
	 * Retrieves the short prefixes in the table. The prefixes are returned
	 * in no specified order.
	 * @return the short prefixes in the table
	 */
	@Nonnull
	public Collection<? extends Word<I>> getShortPrefixes();
	
	/**
	 * Retrieves the long prefixes in the table. The prefixes are returned
	 * in no specified order.
	 * @return the long prefixes in the table
	 */
	@Nonnull
	public Collection<? extends Word<I>> getLongPrefixes();
	
	/**
	 * Retrieves all prefixes (short and long) in the table. The prefixes are
	 * returned in no specified order.
	 * @return all prefixes in the table
	 */
	@Nonnull
	public Collection<? extends Word<I>> getAllPrefixes();
	
	/**
	 * Retrieves all suffixes in the table.
	 * @return all suffixes in the table
	 */
	@Nonnull
	public List<? extends Word<I>> getSuffixes();
	
	/**
	 * Retrieves a suffix by its (column) index.
	 * 
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	@Nonnull
	public Word<I> getSuffix(@Nonnegative int index) throws IndexOutOfBoundsException;
	
	@Nonnull
	public Collection<? extends Row<I,D>> getShortPrefixRows();
	@Nonnull
	public Collection<? extends Row<I,D>> getLongPrefixRows();
	@Nonnull
	public Collection<? extends Row<I,D>> getAllRows();
	
	@Nonnull
	public Row<I,D> getRow(Word<I> prefix) throws NoSuchRowException;				
	
	@Nullable
	public Row<I,D> getSuccessorRow(Row<I,D> spRow, @Nullable I symbol) throws InvalidRowException;
	
	
	public boolean isClosed();
	@Nullable
	public Row<I,D> findUnclosedRow();
	
	public boolean isConsistent(Collection<? extends I> inputs);
	@Nullable
	public Inconsistency<I,D> findInconsistency(Collection<? extends I> inputs);
	
	/**
	 * 
	 * @param inconsistency
	 * @return the suffix (column) index where the contents of the rows differ, or
	 * {@code #NO_DISTINGUISHING_SUFFIX} if the contents of the rows are equal.
	 * @throws NoSuchRowException if the 
	 */
	@Signed
	public int findDistinguishingSuffixIndex(Inconsistency<I,D> inconsistency) throws NoSuchRowException, InvalidRowException;
	
	/**
	 * 
	 * @param inconsistency
	 * @return
	 * @throws NoSuchRowException
	 */
	@Nullable
	public Word<I> findDistinguishingSuffix(Inconsistency<I,D> inconsistency) throws NoSuchRowException, InvalidRowException;
	
	/**
	 * 
	 * @param firstRow the first row
	 * @param secondRow the second row
	 * @return the suffix (column) index where the contents of the rows differ, or
	 * {@code #NO_DISTINGUISHING_SUFFIX} if the contents of the rows are equal.  
	 * @throws InvalidRowException if the rows do not belong to this observation table
	 */
	@Signed
	public int findDistinguishingSuffixIndex(Row<I,D> firstRow, Row<I,D> secondRow) throws InvalidRowException;
	
	/**
	 * 
	 * @param firstRow the first row
	 * @param secondRow the second row
	 * @return the suffix distinguishing the contents of the two rows
	 * @throws InvalidRowException if the rows do not belong to this observation table
	 */
	@Nullable
	public Word<I> findDistinguishingSuffix(Row<I,D> firstRow, Row<I,D> secondRow) throws InvalidRowException;	
}
