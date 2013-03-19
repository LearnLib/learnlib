package de.learnlib.lstar;

import java.util.ArrayList;
import java.util.List;

import net.automatalib.commons.util.comparison.CmpUtil;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.util.Words;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.lstar.table.Row;

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
public abstract class AbstractLStar<A, I, O> implements LearningAlgorithm<A, I, O> {
	
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
	public AbstractLStar(Alphabet<? extends I> alphabet, MembershipOracle<I,O> oracle) {
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
		List<Word<I>> suffixes = initialSuffixes();
		List<List<Row<I>>> initialUnclosed = table.initialize(suffixes, oracle);
		
		completeConsistentTable(initialUnclosed);
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.LearningAlgorithm#refineHypothesis(de.learnlib.api.Query)
	 */
	@Override
	public boolean refineHypothesis(Query<I, O> ceQuery) {
		int oldDistinctRows = table.numDistinctRows();
		
		List<List<Row<I>>> unclosed = incorporateCounterExample(ceQuery);
		completeConsistentTable(unclosed);
		return (table.numDistinctRows() > oldDistinctRows);
	}
	
	/**
	 * Iteratedly checks for unclosedness and inconsistencies in the table,
	 * and fixes any occurrences thereof. This process is repeated until the
	 * observation table is both closed and consistent. 
	 * @param unclosed the unclosed rows (equivalence classes) to start with.
	 */
	protected void completeConsistentTable(List<List<Row<I>>> unclosed) {
		do {
			while(!unclosed.isEmpty()) {
				List<Row<I>> closingRows = selectClosingRows(unclosed);
				unclosed = table.toShortPrefixes(closingRows, oracle);
			}
			
			Inconsistency<I,O> incons = null;
			
			do {
				incons = table.findInconsistency();
				if(incons != null) {
					Word<I> newSuffix = analyzeInconsistency(incons);
					unclosed = table.addSuffix(newSuffix, oracle);
				}
			} while(unclosed.isEmpty() && (incons != null));
		} while(!unclosed.isEmpty());
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
			// FIXME: Allow null values?
			if(!CmpUtil.equals(val1, val2)) {
				I sym = alphabet.getSymbol(inputIdx);
				Word<I> suffix = table.getSuffixes().get(i);
				return Words.prepend(sym, suffix);
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
	protected List<List<Row<I>>> incorporateCounterExample(Query<I,O> ce) {
		Word<I> ceWord = ce.getInput();
		
		List<Word<I>> newPrefixes = new ArrayList<Word<I>>(ceWord.size() - 2);
		
		for(int i = 1; i <= ceWord.size(); i++) {
			Word<I> prefix = Words.prefix(ceWord, i);
			newPrefixes.add(prefix);
		}
		
		
		
		return table.addShortPrefixes(newPrefixes, oracle);
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

}
