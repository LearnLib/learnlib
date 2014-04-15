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
package de.learnlib.algorithms.lstargeneric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.learnlib.algorithms.features.globalsuffixes.GlobalSuffixLearner;
import de.learnlib.algorithms.features.observationtable.OTLearner;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.table.Inconsistency;
import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * An abstract base class for L*-style algorithms.
 * 
 * This class implements basic management features (table, alphabet, oracle) and
 * the main loop of alternating completeness and consistency checks. It does not take
 * care of choosing how to initialize the table and hypothesis construction.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <A> automaton class.
 * @param <I> input symbol class.
 * @param <O> output class.
 */
public abstract class AbstractLStar<A, I, O> implements OTLearner<A, I, O>, GlobalSuffixLearner<A, I, O> {
	
	protected final Alphabet<? extends I> alphabet;
	protected final MembershipOracle<I, O> oracle;
	protected final ObservationTable<I, O> table;

	/**
	 * Constructor.
	 * @param alphabet the learning alphabet.
	 * @param oracle the membership oracle.
	 * @param outputMapping a mapping that translates between oracle outputs and data entries stored
	 * in the observation table.
	 */
	public AbstractLStar(Alphabet<I> alphabet, MembershipOracle<I,O> oracle) {
		this.alphabet = alphabet;
		this.oracle = oracle;
		
		this.table = new ObservationTable<>(alphabet);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#start()
	 */
	@Override
	public void startLearning() {
		List<Word<I>> prefixes = initialPrefixes();
		List<Word<I>> suffixes = initialSuffixes();
		List<List<Row<I>>> initialUnclosed = table.initialize(prefixes, suffixes, oracle);
		
		completeConsistentTable(initialUnclosed, table.isInitialConsistencyCheckRequired());
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.api.Query)
	 */
	@Override
	public final boolean refineHypothesis(DefaultQuery<I, O> ceQuery) {
		int oldDistinctRows = table.numDistinctRows();
		doRefineHypothesis(ceQuery);
		return (table.numDistinctRows() > oldDistinctRows);
	}
	
	protected void doRefineHypothesis(DefaultQuery<I,O> ceQuery) {
		List<List<Row<I>>> unclosed = incorporateCounterExample(ceQuery);
		completeConsistentTable(unclosed, true);
	}
	
	/**
	 * Iteratedly checks for unclosedness and inconsistencies in the table,
	 * and fixes any occurrences thereof. This process is repeated until the
	 * observation table is both closed and consistent. 
	 * @param unclosed the unclosed rows (equivalence classes) to start with.
	 */
	protected boolean completeConsistentTable(List<List<Row<I>>> unclosed, boolean checkConsistency) {
		boolean refined = false;
		do {
			while(!unclosed.isEmpty()) {
				List<Row<I>> closingRows = selectClosingRows(unclosed);
				unclosed = table.toShortPrefixes(closingRows, oracle);
				refined = true;
			}
			
			
			if(checkConsistency) {
				Inconsistency<I,O> incons = null;
				
				do {
					incons = table.findInconsistency();
					if(incons != null) {
						Word<I> newSuffix = analyzeInconsistency(incons);
						unclosed = table.addSuffix(newSuffix, oracle);
					}
				} while(unclosed.isEmpty() && (incons != null));
			}
		} while(!unclosed.isEmpty());
		
		return refined;
	}
	
	
	/**
	 * Analyzes an inconsistency. This analysis consists in determining
	 * the column in which the two successor rows differ.
	 * @param incons the inconsistency description
	 * @return the suffix to add in order to fix the inconsistency
	 */
	protected Word<I> analyzeInconsistency(Inconsistency<I,O> incons) {
		int inputIdx = incons.getInputIndex();
		
		Row<I> succRow1 = incons.getFirstRow().getSuccessor(inputIdx);
		Row<I> succRow2 = incons.getSecondRow().getSuccessor(inputIdx);
		
		int numSuffixes = table.numSuffixes();
		
		List<O> contents1 = table.rowContents(succRow1);
		List<O> contents2 = table.rowContents(succRow2);
		
		for(int i = 0; i < numSuffixes; i++) {
			O val1 = contents1.get(i), val2 = contents2.get(i);
			if(!Objects.equals(val1, val2)) {
				I sym = alphabet.getSymbol(inputIdx);
				Word<I> suffix = table.getSuffixes().get(i);
				return suffix.prepend(sym);
			}
		}
		
		throw new IllegalArgumentException("Bogus inconsistency");
	}


	/**
	 * Incorporates the information provided by a counterexample into
	 * the observation data structure.
	 * @param ce the query which contradicts the hypothesis
	 * @return the rows (equivalence classes) which became unclosed by
	 * adding the information. 
	 */
	protected List<List<Row<I>>> incorporateCounterExample(DefaultQuery<I,O> ce) {
		return ObservationTableCEXHandlers.handleClassicLStar(ce, table, oracle);
	}
	
	/**
	 * This method selects a set of rows to use for closing the table.
	 * It receives as input a list of row lists, such that each (inner) list contains
	 * long prefix rows with (currently) identical contents, which have no matching
	 * short prefix row. The outer list is the list of all those equivalence classes.
	 * 
	 * @param unclosed a list of equivalence classes of unclosed rows.
	 * @return a list containing a representative row from each class to move
	 * to the short prefix part.
	 */
	protected List<Row<I>> selectClosingRows(List<List<Row<I>>> unclosed) {
		List<Row<I>> closingRows = new ArrayList<Row<I>>(unclosed.size());
		
		for(List<Row<I>> rowList : unclosed)
			closingRows.add(rowList.get(0));
		
		return closingRows;
	}
	
	
	/**
	 * Returns the list of initial suffixes which are used to initialize the table.
	 * @return the list of initial suffixes.
	 */
	protected abstract List<Word<I>> initialSuffixes();
	
	protected List<Word<I>> initialPrefixes() {
		return Collections.singletonList(Word.<I>epsilon());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.features.GlobalSuffixLearner#getGlobalSuffixes()
	 */
	@Override
	public Collection<? extends Word<I>> getGlobalSuffixes() {
		return Collections.unmodifiableCollection(table.getSuffixes());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.features.GlobalSuffixLearner#addGlobalSuffixes(java.util.Collection)
	 */
	@Override
	public boolean addGlobalSuffixes(Collection<? extends Word<I>> newGlobalSuffixes) {
		List<List<Row<I>>> unclosed = table.addSuffixes(newGlobalSuffixes, oracle);
		if(unclosed.isEmpty()) {
			return false;
		}
		return completeConsistentTable(unclosed, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.algorithms.features.observationtable.OTLearner#getObservationTable()
	 */
	@Override
	public de.learnlib.algorithms.features.observationtable.ObservationTable<I, O> getObservationTable() {
		return table.asStandardTable();
	}
}
