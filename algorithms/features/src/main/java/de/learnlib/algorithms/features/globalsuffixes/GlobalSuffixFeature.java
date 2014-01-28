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
package de.learnlib.algorithms.features.globalsuffixes;

import java.util.Collection;

import net.automatalib.words.Word;

public interface GlobalSuffixFeature<I> {
	/**
	 * Retrieves the global suffixes of this learner. Calling this method before {@link #startLearning()}
	 * should return an empty collection.
	 * <p>
	 * The return value should not be modified; attempting to do so may result in an
	 * {@link UnsupportedOperationException}. It is the implementation's responsibility
	 * to ensure attempted modifications do not corrupt the learner's internal state.
	 *  
	 * @return the global suffixes used by this algorithm
	 */
	public Collection<? extends Word<I>> getGlobalSuffixes();
	
	/**
	 * Add the provided suffixes to the collection of global suffixes. As this method 
	 * is designed to possibly trigger a <em>refinement</em>, it is illegal to invoke
	 * it before {@link #startLearning()} has been called.
	 * <p>
	 * The implementation may choose to (but is not required to) omit suffixes which are
	 * already present (that is, manage the global suffixes as a proper set).
	 * 
	 * @param globalSuffixes the global suffixes to add
	 * @return {@code true} if a refinement was triggered by adding the global suffixes,
	 * {@code false otherwise}.
	 */
	public boolean addGlobalSuffixes(Collection<? extends Word<I>> globalSuffixes);
}
