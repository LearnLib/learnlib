package de.learnlib.lstar;

import de.learnlib.lstar.table.Row;

/**
 * A description of an inconsistency in an {@link ObservationTable}. An inconsistency
 * consists of two short prefixes <code>u</code>, <code>u'</code> with identical contents,
 * and an input symbol <code>a</code>, such that the rows for <code>ua</code> and <code>u'a</code>
 * have different contents.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output class
 */
public class Inconsistency<I, O> {
	private final Row<I> firstRow;
	private final Row<I> secondRow;
	private final int inputIndex;
	
	/**
	 * Constructor.
	 * @param firstRow the first row
	 * @param secondRow the second row
	 * @param inputIndex the input symbol for which the successor rows differ
	 */
	public Inconsistency(Row<I> firstRow, Row<I> secondRow, int inputIndex) {
		this.firstRow = firstRow;
		this.secondRow = secondRow;
		this.inputIndex = inputIndex;
	}
	
	/**
	 * Retrieves the first row.
	 * @return the first row
	 */
	public Row<I> getFirstRow() {
		return firstRow;
	}
	
	/**
	 * Retrieves the second row.
	 * @return the second row
	 */
	public Row<I> getSecondRow() {
		return secondRow;
	}
	
	/**
	 * Retrieves the index of the input symbol for which the successor rows differ.
	 * @return the input symbol index
	 */
	public int getInputIndex() {
		return inputIndex;
	}
}
