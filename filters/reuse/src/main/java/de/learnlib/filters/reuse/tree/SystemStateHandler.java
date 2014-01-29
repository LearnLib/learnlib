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
package de.learnlib.filters.reuse.tree;

import de.learnlib.filters.reuse.ReuseOracle.ReuseOracleBuilder;

/**
 * A implementation of this interface that is set to the {@link ReuseTree} 
 * (see {@link ReuseOracleBuilder#withSystemStateHandler(SystemStateHandler)}) 
 * will be informed about all removed system states whenever
 * {@link ReuseTree#disposeSystemstates()} gets called.
 * <p>
 * The objective of this handler is that clearing system states from the reuse
 * tree may also be resulting in cleaning up the SUL by e.g. perform tasks like
 * removing persisted entities from a database.
 * <p>
 * Please note that the normal removal of system states (by sifting them down in
 * the reuse tree by executing only suffixes of a query) is not be seen as a
 * disposing.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * 
 * @param <S> system state class
 */
public interface SystemStateHandler<S> {
	/**
	 * The system state S will be removed from the {@link ReuseTree}.
	 *
	 * @param state
	 *      The state to remove.
	 */
	void dispose(S state);
}
