/* Copyright (C) 2013-2014 TU Dortmund
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
package de.learnlib.api;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.automatalib.words.Word;
import de.learnlib.oracles.DefaultQuery;

/**
 * Membership oracle interface. A membership oracle provides an elementary abstraction
 * to a System Under Learning (SUL), by allowing to pose {@link DefaultQuery}s: A query is a sequence
 * of input symbols (divided into a prefix and a suffix part, cf. {@link DefaultQuery#getPrefix()}
 * and {@link DefaultQuery#getSuffix()}, in reaction to which the SUL produces a specific observable
 * behavior (outputting a word, acceptance/rejection etc.).
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 * @author Maik Merten <maikmerten@googlemail.com>
 * 
 * @see DefaultQuery
 */
public interface MembershipOracle<I, O> {
	
	public static interface DFAMembershipOracle<I> extends MembershipOracle<I,Boolean> {}
	public static interface MealyMembershipOracle<I,O> extends MembershipOracle<I,Word<O>> {}
	
	/**
	 * Processes the specified collection of queries. When this method returns,
	 * the output field of each of the contained queries should reflect the SUL
	 * response to the respective query.
	 * 
	 * @param queries the queries to process
	 * @see DefaultQuery#getOutput()
	 */
	public void processQueries(@Nonnull Collection<? extends Query<I, O>> queries);
}
