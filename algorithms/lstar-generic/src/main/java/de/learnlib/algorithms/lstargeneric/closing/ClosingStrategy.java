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
package de.learnlib.algorithms.lstargeneric.closing;

import java.util.List;

import de.learnlib.algorithms.lstargeneric.table.ObservationTable;
import de.learnlib.algorithms.lstargeneric.table.Row;
import de.learnlib.api.MembershipOracle;

/**
 * A closing strategy, determining how to proceed when an observation table needs to be closed.
 * 
 * @author Malte Isberner
 *
 * @param <I> type variable for input symbol upper bound.
 * @param <D> type variable for output symbol upper bound.
 */
public interface ClosingStrategy<I, D> {
	/**
	 * Given a list of row equivalence classes, this method selects for each of the classes
	 * one (representative) row which is being closed. This corresponds to selecting one of several
	 * long prefixes (i.e., transitions reaching an unknown state) to be an access sequence.
	 * <p>
	 * By contract, the size of the the returned list <b>must</b> equal the size of the
	 * {@code unclosedClasses} argument.
	 * 
	 * @param unclosedClasses the list of row equivalence classes
	 * @param table the observation table
	 * @param oracle the membership oracle
	 * @return a selection of representative rows to be closed.
	 */
	<RI extends I,RO extends D>
	List<Row<RI>> selectClosingRows(List<List<Row<RI>>> unclosedClasses, ObservationTable<RI,RO> table,
			MembershipOracle<RI,RO> oracle);
}
