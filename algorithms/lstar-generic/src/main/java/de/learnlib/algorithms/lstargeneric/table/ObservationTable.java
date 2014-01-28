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
package de.learnlib.algorithms.lstargeneric.table;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import de.learnlib.algorithms.features.observationtable.AbstractObservationTable;
import de.learnlib.algorithms.features.observationtable.AbstractObservationTable.AbstractRow;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;


/**
 * Observation table class.
 * <p>
 * An observation table (OT) is the central data structure used by Angluin's L* algorithm,
 * as described in the paper "Learning Regular Sets from Queries and Counterexamples".
 * <p>
 * An observation table is a two-dimensional table, with rows indexed by prefixes,
 * and columns indexed by suffixes. For a prefix <code>u</code> and a suffix <code>v</code>,
 * the respective cell contains the result of the membership query <code>(u, v)</code>.
 * <p>
 * The set of prefixes (row labels) is divided into two disjoint sets: short and long prefixes.
 * Each long prefix is a one-letter extension of a short prefix; conversely, every time a
 * prefix is added to the set of short prefixes, all possible one-letter extensions are added
 * to the set of long prefixes.
 * <p>
 * In order to derive a well-defined hypothesis from an observation table, it must satisfy two
 * properties: closedness and consistency.
 * <ul>
 * <li>An observation table is <b>closed</b> iff for each long prefix <code>u</code> there exists
 * a short prefix <code>u'</code> such that the row contents for both prefixes are equal.
 * <li>An observation table is <b>consistent</b> iff for every two short prefixes <code>u</code> and
 * <code>u'</code> with identical row contents, it holds that for every input symbol <code>a</code>
 * the rows indexed by <code>ua</code> and <code>u'a</code> also have identical contents.  
 * </ul>
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output class
 */
public final class ObservationTable<I,O> implements AccessSequenceTransformer<I> {
	
	// private static final Integer NO_ENTRY = null;
	private static final int NO_ENTRY = -1;
	
	
	private final Alphabet<I> alphabet;
	
	private final List<Row<I>> shortPrefixRows
		= new ArrayList<Row<I>>();
	private final List<Row<I>> longPrefixRows
		= new ArrayList<Row<I>>();
	
	private final List<Row<I>> allRows
		= new ArrayList<Row<I>>();
	
	private final List<List<O>> allRowContents
		= new ArrayList<List<O>>();
	
	private final List<Row<I>> canonicalRows
		= new ArrayList<Row<I>>();
	
	/*
	private final Map<List<O>,Integer> rowContentIds
		= new HashMap<List<O>,Integer>();
	*/
	
	private final TObjectIntMap<List<O>> rowContentIds
		= new TObjectIntHashMap<>(10, 0.75f, NO_ENTRY);
	
	private final Map<Word<I>,Row<I>> rowMap
		= new HashMap<Word<I>,Row<I>>();
	
	private int numRows = 0;
	
	private final List<Word<I>> suffixes
		= new ArrayList<Word<I>>();
	
	
	/**
	 * Constructor.
	 * @param alphabet the learning alphabet.
	 */
	public ObservationTable(Alphabet<I> alphabet) {
		this.alphabet = alphabet;
	}
	
	/**
	 * Initializes an observation table using a specified set of suffixes.
	 * 
	 * @param initialSuffixes the set of initial column labels.
	 * @param oracle the {@link MembershipOracle} to use for performing queries
	 * @return a list of equivalence classes of unclosed rows
	 */
	public List<List<Row<I>>> initialize(List<Word<I>> initialSuffixes, MembershipOracle<I,O> oracle) {
		if(allRows.size() > 0)
			throw new IllegalStateException("Called initialize, but there are already rows present");
		
		int numSuffixes = initialSuffixes.size();
		suffixes.addAll(initialSuffixes);
		
		int numLps = alphabet.size();
		int numPrefixes = 1 + numLps;
		
		List<DefaultQuery<I,O>> queries = new ArrayList<DefaultQuery<I,O>>(numPrefixes * numSuffixes);
		
		Word<I> eps = Word.epsilon();
		Row<I> epsRow = createSpRow(Word.<I>epsilon());
		
		buildQueries(queries, eps, suffixes);
		
		for(int i = 0; i < alphabet.size(); i++) {
			I sym = alphabet.getSymbol(i);
			Word<I> w = Word.fromLetter(sym);
			Row<I> lpRow = createLpRow(w);
			buildQueries(queries, w, suffixes);
			epsRow.setSuccessor(i, lpRow);
		}
		
		oracle.processQueries(queries);
		
		Iterator<DefaultQuery<I,O>> queryIt = queries.iterator();
		
		List<O> firstRowContents = new ArrayList<O>(numSuffixes);
		fetchResults(queryIt, firstRowContents, numSuffixes);
		processContents(epsRow, firstRowContents, true);
		
		List<List<Row<I>>> unclosed = new ArrayList<List<Row<I>>>();
		
		for(Row<I> lpRow : longPrefixRows) {
			List<O> rowContents = new ArrayList<O>(numSuffixes);
			fetchResults(queryIt, rowContents, numSuffixes);
			if(processContents(lpRow, rowContents, false))
				unclosed.add(new ArrayList<Row<I>>());
			
			int id = lpRow.getRowContentId();
			
			if(id > 0)
				unclosed.get(id - 1).add(lpRow);
		}
		
		return unclosed;
	}
	
	/**
	 * Adds a suffix to the list of distinguishing suffixes. This is a convenience method
	 * that can be used as shorthand for
	 * <code>addSufixes(Collections.singletonList(suffix), oracle)</code>
	 * @param suffix the suffix to add
	 * @param oracle the membership oracle
	 * @return a list of equivalence classes of unclosed rows
	 */
	public List<List<Row<I>>> addSuffix(Word<I> suffix, MembershipOracle<I,O> oracle) {
		return addSuffixes(Collections.singletonList(suffix), oracle);
	}
	
	/**
	 * Adds suffixes to the list of distinguishing suffixes.
	 * @param newSuffixes the suffixes to add
	 * @param oracle the membership oracle
	 * @return a list of equivalence classes of unclosed rows
	 */
	public List<List<Row<I>>> addSuffixes(Collection<? extends Word<I>> newSuffixes, MembershipOracle<I, O> oracle) {
		int oldSuffixCount = suffixes.size();
		// we need a stable iteration order, and only List guarantees this
		List<? extends Word<I>> newSuffixList;
		if(newSuffixes instanceof List) {
			newSuffixList = (List<? extends Word<I>>)newSuffixes;
		}
		else {
			newSuffixList = new ArrayList<>(newSuffixes);
		}
		
		int numNewSuffixes = newSuffixList.size();
		
		int numSpRows = shortPrefixRows.size();
		int rowCount = numSpRows + longPrefixRows.size();
		
		List<DefaultQuery<I,O>> queries = new ArrayList<DefaultQuery<I,O>>(rowCount * numNewSuffixes);
		
		for(Row<I> row : shortPrefixRows)
			buildQueries(queries, row.getPrefix(), newSuffixList);
		
		for(Row<I> row : longPrefixRows)
			buildQueries(queries, row.getPrefix(), newSuffixList);
		
		oracle.processQueries(queries);
		
		Iterator<DefaultQuery<I,O>> queryIt = queries.iterator();
		
		for(Row<I> row : shortPrefixRows) {
			List<O> rowContents = allRowContents.get(row.getRowContentId());
			if(rowContents.size() == oldSuffixCount) {
				rowContentIds.remove(rowContents);
				fetchResults(queryIt, rowContents, numNewSuffixes);
				rowContentIds.put(rowContents, row.getRowContentId());
			}
			else {
				List<O> newContents = new ArrayList<O>(oldSuffixCount + numNewSuffixes);
				newContents.addAll(rowContents.subList(0, oldSuffixCount));
				fetchResults(queryIt, newContents, numNewSuffixes);
				processContents(row, newContents, true);
			}
		}
		
		List<List<Row<I>>> unclosed = new ArrayList<List<Row<I>>>();
		numSpRows = numDistinctRows();
		
		for(Row<I> row : longPrefixRows) {
			List<O> rowContents = allRowContents.get(row.getRowContentId());
			if(rowContents.size() == oldSuffixCount) {
				rowContentIds.remove(rowContents);
				fetchResults(queryIt, rowContents, numNewSuffixes);
				rowContentIds.put(rowContents, row.getRowContentId());
			}
			else {
				List<O> newContents = new ArrayList<O>(oldSuffixCount + numNewSuffixes);
				newContents.addAll(rowContents.subList(0, oldSuffixCount));
				fetchResults(queryIt, newContents, numNewSuffixes);
				if(processContents(row, newContents, false))
					unclosed.add(new ArrayList<Row<I>>());
				
				int id = row.getRowContentId();
				if(id >= numSpRows)
					unclosed.get(id - numSpRows).add(row);
			}	
		}
		
		this.suffixes.addAll(newSuffixList);
		
		return unclosed;
	}
	
	
	/**
	 * Moves the specified rows to the set of short prefix rows. If some of the specified
	 * rows already are short prefix rows, they are ignored (unless they do not have any
	 * contents, in which case they are completed).
	 * @param lpRows the rows to move to the set of short prefix rows
	 * @param oracle the membership oracle
	 * @return a list of equivalence classes of unclosed rows
	 */
	public List<List<Row<I>>> toShortPrefixes(List<Row<I>> lpRows, MembershipOracle<I,O> oracle) {
		List<Row<I>> freshSpRows = new ArrayList<Row<I>>();
		List<Row<I>> freshLpRows = new ArrayList<Row<I>>();
		
		for(Row<I> row : lpRows) {
			if(row.isShortPrefix()) {
				if(row.hasContents())
					continue;
				freshSpRows.add(row);
			}
			else {
				makeShort(row);
				if(!row.hasContents())
					freshSpRows.add(row);
			}
			
			Word<I> prefix = row.getPrefix();
						
			for(int i = 0; i < alphabet.size(); i++) {
				I sym = alphabet.getSymbol(i);
				Word<I> lp = prefix.append(sym);
				Row<I> lpRow = rowMap.get(lp);
				if(lpRow == null) {
					lpRow = createLpRow(lp);
					freshLpRows.add(lpRow);
				}
				row.setSuccessor(i, lpRow);
			}
		}
		
		int numSuffixes = suffixes.size();
		
		int numFreshRows = freshSpRows.size() + freshLpRows.size();
		List<DefaultQuery<I,O>> queries = new ArrayList<DefaultQuery<I,O>>(numFreshRows * numSuffixes);
		buildRowQueries(queries, freshSpRows, suffixes);
		buildRowQueries(queries, freshLpRows, suffixes);
		
		oracle.processQueries(queries);
		Iterator<DefaultQuery<I,O>> queryIt = queries.iterator();
		
		for(Row<I> row : freshSpRows) {
			List<O> contents = new ArrayList<O>(numSuffixes);
			fetchResults(queryIt, contents, numSuffixes);
			processContents(row, contents, true);
		}
		
		int numSpRows = numDistinctRows();
		List<List<Row<I>>> unclosed = new ArrayList<List<Row<I>>>();
		
		for(Row<I> row : freshLpRows) {
			List<O> contents = new ArrayList<O>(numSuffixes);
			fetchResults(queryIt, contents, numSuffixes);
			if(processContents(row, contents, false))
				unclosed.add(new ArrayList<Row<I>>());
			
			int id = row.getRowContentId();
			if(id >= numSpRows)
				unclosed.get(id - numSpRows).add(row);
		}
		
		return unclosed;
	}
	
	public Row<I> getRowSuccessor(Row<I> row, I sym) {
		return row.getSuccessor(alphabet.getSymbolIndex(sym));
	}
	
	public List<List<Row<I>>> addShortPrefixes(List<? extends Word<I>> shortPrefixes, MembershipOracle<I,O> oracle) {	
		List<Row<I>> toSpRows = new ArrayList<Row<I>>();
		
		for(Word<I> sp : shortPrefixes) {
			Row<I> row = rowMap.get(sp);
			if(row != null) {
				if(row.isShortPrefix())
					continue;
			}
			else
				row = createSpRow(sp);
			toSpRows.add(row);
		}
		
		return toShortPrefixes(toSpRows, oracle);
	}
	
	
	@SuppressWarnings("unchecked")
	public Inconsistency<I,O> findInconsistency() {
		Row<I>[] canonicalRows = (Row<I>[])new Row<?>[numDistinctRows()];
		
		for(Row<I> spRow : shortPrefixRows) {
			int contentId = spRow.getRowContentId();
			
			Row<I> canRow = canonicalRows[contentId];
			if(canRow == null) {
				canonicalRows[contentId] = spRow;
				continue;
			}
			
			for(int i = 0; i < alphabet.size(); i++) {
				int spSuccContent = spRow.getSuccessor(i).getRowContentId();
				int canSuccContent = canRow.getSuccessor(i).getRowContentId();
				if(spSuccContent != canSuccContent)
					return new Inconsistency<I,O>(canRow, spRow, i);
			}
		}
		
		return null;
	}
	
	
	protected boolean makeShort(Row<I> row) {
		if(row.isShortPrefix())
			return false;
		
		int lastIdx = longPrefixRows.size() - 1;
		Row<I> last = longPrefixRows.get(lastIdx);
		int rowIdx = row.getLpIndex();
		longPrefixRows.remove(lastIdx);
		if(last != row) {
			longPrefixRows.set(rowIdx, last);
			last.setLpIndex(rowIdx);
		}
		
		shortPrefixRows.add(row);
		row.makeShort(alphabet.size());
		
		if(row.hasContents()) {
			int cid = row.getRowContentId();
			if(canonicalRows.get(cid) == null)
				canonicalRows.set(cid, row);
		}
		return true;
	}
	
	protected Row<I> createLpRow(Word<I> prefix) {
		Row<I> newRow = new Row<I>(prefix, numRows++);
		allRows.add(newRow);
		rowMap.put(prefix, newRow);
		int idx = longPrefixRows.size();
		longPrefixRows.add(newRow);
		newRow.setLpIndex(idx);
		return newRow;
	}
	
	protected Row<I> createSpRow(Word<I> prefix) {
		Row<I> newRow = new Row<I>(prefix, numRows++, alphabet.size());
		allRows.add(newRow);
		rowMap.put(prefix, newRow);
		shortPrefixRows.add(newRow);
		return newRow;
	}
	
	public List<O> rowContents(Row<I> row) {
		return allRowContents.get(row.getRowContentId());
	}
	
	public O cellContents(Row<I> row, int columnId) {
		List<O> contents = rowContents(row);
		return contents.get(columnId);
	}
	
	public Row<I> getRow(int rowId) {
		return allRows.get(rowId);
	}
	
	public int numDistinctRows() {
		return allRowContents.size();
	}
	
	public List<Row<I>> getShortPrefixRows() {
		return shortPrefixRows;
	}

	public int numShortPrefixRows() {
		return shortPrefixRows.size();
	}
	
	public int numLongPrefixRows() {
		return longPrefixRows.size();
	}
	
	public int numTotalRows() {
		return shortPrefixRows.size() + longPrefixRows.size();
	}
	
	public int numSuffixes() {
		return suffixes.size();
	}
	
	public List<Word<I>> getSuffixes() {
		return suffixes;
	}
	
	protected boolean processContents(Row<I> row, List<O> rowContents, boolean makeCanonical) {
		// Integer contentId;
		int contentId;
		boolean added = false;
		if((contentId = rowContentIds.get(rowContents)) == NO_ENTRY) {
			rowContentIds.put(rowContents, contentId = numDistinctRows());
			allRowContents.add(rowContents);
			added = true;
			if(makeCanonical)
				canonicalRows.add(row);
			else
				canonicalRows.add(null);
		}
		row.setRowContentId(contentId);
		return added;
	}
	
	protected static <I,O>
	void buildQueries(List<DefaultQuery<I,O>> queryList, List<Word<I>> prefixes, List<? extends Word<I>> suffixes) {
		for(Word<I> prefix : prefixes)
			buildQueries(queryList, prefix, suffixes);
	}
	
	protected static <I,O>
	void buildRowQueries(List<DefaultQuery<I,O>> queryList, List<Row<I>> rows, List<? extends Word<I>> suffixes) {
		for(Row<I> row : rows)
			buildQueries(queryList, row.getPrefix(), suffixes);
	}
	
	protected static <I,O>
	void buildQueries(List<DefaultQuery<I,O>> queryList, Word<I> prefix, List<? extends Word<I>> suffixes) {
		for(Word<I> suffix : suffixes)
			queryList.add(new DefaultQuery<I,O>(prefix, suffix));
	}
	
	/**
	 * Fetches the given number of query responses and adds them to the specified output list.
	 * Also, the query iterator is advanced accordingly.
	 * @param queryIt the query iterator
	 * @param output the output list to write to
	 * @param numSuffixes the number of suffixes (queries)
	 */
	protected static <I,O>
	void fetchResults(Iterator<DefaultQuery<I,O>> queryIt, List<O> output, int numSuffixes) {
		for(int j = 0; j < numSuffixes; j++) {
			DefaultQuery<I,O> qry = queryIt.next();
			output.add(qry.getOutput());
		}
	}

	/**
	 * Checks whether this observation table has been initialized yet (i.e., contains any rows).
	 * @return <tt>true</tt> iff the table has been initialized
	 */
	public boolean isInitialized() {
		return (allRows.size() > 0);
	}

	/**
	 * Retrieves the input alphabet used in this observation table.
	 * @return the input alphabet
	 */
	public Alphabet<I> getInputAlphabet() {
		return alphabet;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#transformAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public Word<I> transformAccessSequence(Word<I> word) {
		Row<I> current = shortPrefixRows.get(0);
		
		for(I sym : word) {
			current = getRowSuccessor(current, sym);
			if(!current.isShortPrefix())
				current = canonicalRows.get(current.getRowContentId());
		}
		
		return current.getPrefix();
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.AccessSequenceTransformer#isAccessSequence(net.automatalib.words.Word)
	 */
	@Override
	public boolean isAccessSequence(Word<I> word) {
		Row<I> current = shortPrefixRows.get(0);
		
		for(I sym : word) {
			current = getRowSuccessor(current, sym);
			if(!current.isShortPrefix())
				return false;
		}
		
		return true;
	}
	
	private class StandardRowWrapper extends AbstractRow<I, O> {
		private final Row<I> internalRow;
		
		public StandardRowWrapper(Row<I> internalRow) {
			this.internalRow = internalRow;
		}
		@Override
		public Word<I> getLabel() {
			return internalRow.getPrefix();
		}
		@Override
		public boolean isShortPrefixRow() {
			return internalRow.isShortPrefix();
		}
		@Override
		public List<O> getContents() {
			return Collections.unmodifiableList(allRowContents.get(internalRow.getRowContentId()));
		}
	}
	
	private StandardRowWrapper wrapRow(Row<I> internalRow) {
		if(internalRow != null) {
			return new StandardRowWrapper(internalRow);
		}
		return null;
	}
	
	public de.learnlib.algorithms.features.observationtable.ObservationTable<I, O> asStandardTable() {
		final Function<Row<I>,StandardRowWrapper> wrapRow = new Function<Row<I>,StandardRowWrapper>() {
			@Override
			public StandardRowWrapper apply(Row<I> internalRow) {
				return wrapRow(internalRow);
			}
		};
		
		return new AbstractObservationTable<I, O>() {
			@Override
			public List<? extends Word<I>> getSuffixes() {
				return Collections.unmodifiableList(suffixes);
			}
			@Override
			public Collection<? extends de.learnlib.algorithms.features.observationtable.ObservationTable.Row<I, O>> getShortPrefixRows() {
				return Collections.unmodifiableList(Lists.transform(shortPrefixRows, wrapRow));
			}

			@Override
			public Collection<? extends de.learnlib.algorithms.features.observationtable.ObservationTable.Row<I, O>> getLongPrefixRows() {
				return Collections.unmodifiableList(Lists.transform(longPrefixRows, wrapRow));
			}

			@Override
			public de.learnlib.algorithms.features.observationtable.ObservationTable.Row<I, O> getSuccessorRow(
					de.learnlib.algorithms.features.observationtable.ObservationTable.Row<I, O> spRow,
					I symbol) {
				if(!(spRow instanceof ObservationTable.StandardRowWrapper)) {
					throw new IllegalArgumentException("Invalid observation table row");
				}
				StandardRowWrapper wrapped = (StandardRowWrapper)spRow;
				return wrapRow(getRowSuccessor(wrapped.internalRow, symbol));
			}
			@Override
			public List<? extends de.learnlib.algorithms.features.observationtable.ObservationTable.Row<I, O>> getAllRows() {
				return Collections.unmodifiableList(Lists.transform(allRows, wrapRow));
			}
			@Override
			public de.learnlib.algorithms.features.observationtable.ObservationTable.Row<I, O> getRow(
					Word<I> prefix) {
				return wrapRow(rowMap.get(prefix));
			}
			@Override
			public Collection<? extends Word<I>> getAllPrefixes() {
				return Collections.unmodifiableSet(rowMap.keySet());
			}
			
			
		};
	}
}
