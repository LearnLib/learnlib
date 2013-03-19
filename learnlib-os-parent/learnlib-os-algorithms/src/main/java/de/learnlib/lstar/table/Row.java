package de.learnlib.lstar.table;

import net.automatalib.words.Word;


/**
 * A row in an observation table. Minimally, a row consists of a prefix (the row label)
 * and a unique identifier in its observation table which remains constant throughout the
 * whole process.
 * 
 * Apart from that, a row is also associated with contents (via an integer id). The prefix of a row
 * may be either a short or long prefix. In the former case, the row will also have successor rows
 * (one-step futures) associated with it.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 */
public class Row<I> {
	private final Word<I> prefix;
	private final int rowId;
	
	private int rowContentId = -1;
	private boolean shortPrefix = false;
	private Row<I>[] successors = null; 
	
	/**
	 * Constructor.
	 * @param prefix the prefix (label) of this row 
	 * @param rowId the unique row identifier
	 */
	public Row(Word<I> prefix, int rowId) {
		this.prefix = prefix;
		this.rowId = rowId;
	}
	
	/**
	 * Constructor for short prefix rows. 
	 * @param prefix the prefix (label) of this row
	 * @param rowId the unique row identifier
	 * @param alphabetSize the size of the alphabet, used for initializing the successor array
	 */
	public Row(Word<I> prefix, int rowId, int alphabetSize) {
		this(prefix, rowId);
		
		makeShort(alphabetSize);
	}
	
	/**
	 * Makes this row a short prefix row. This leads to a successor array being created.
	 * If this row already is a short prefix row, nothing happens.
	 * @param alphabetSize the size of the input alphabet.
	 */
	@SuppressWarnings("unchecked")
	public void makeShort(int alphabetSize) {
		if(shortPrefix)
			return;
		shortPrefix = true;
		this.successors = (Row<I>[])new Row<?>[alphabetSize];
	}
	
	/**
	 * Retrieves the successor row for this short prefix row and the given alphabet
	 * symbol (by index). If this is no short prefix row, an exception might occur.
	 * @param inputIdx the index of the alphabet symbol.
	 * @return the successor row (may be <code>null</code>)
	 */
	public Row<I> getSuccessor(int inputIdx) {
		return successors[inputIdx];
	}
	
	/**
	 * Sets the successor row for this short prefix row and the given alphabet symbol
	 * (by index). If this is no short prefix row, an exception might occur.
	 * @param inputIdx the index of the alphabet symbol.
	 * @param succ the successor row
	 */
	public void setSuccessor(int inputIdx, Row<I> succ) {
		successors[inputIdx] = succ;
	}
	
	/**
	 * Retrieves the prefix (row label) associated with this row.
	 * @return the prefix
	 */
	public Word<I> getPrefix() {
		return prefix;
	}
	
	/**
	 * Retrieves the unique row identifier associated with this row.
	 * @return the row identifier
	 */
	public int getRowId() {
		return rowId;
	}
	
	/**
	 * Retrieves the ID of the row contents (may be <code>-1</code> if this row has not
	 * yet been initialized).
	 * @return the contents id.
	 */
	public int getRowContentId() {
		return rowContentId;
	}
	
	/**
	 * Sets the ID of the row contents.
	 * @param id the contents id
	 */
	public void setRowContentId(int id) {
		this.rowContentId = id;
	}
	
	/**
	 * Retrieves whether this is a short prefix row.
	 * @return <code>true</code> if this is a short prefix row, <code>false</code> otherwise.
	 */
	public boolean isShortPrefix() {
		return shortPrefix;
	}

	public boolean hasContents() {
		return (rowContentId != -1);
	}
	
}
