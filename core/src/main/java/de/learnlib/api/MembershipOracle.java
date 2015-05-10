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
import java.util.Collections;

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.oracles.DefaultQuery;

/**
 * Membership oracle interface. A membership oracle provides an elementary abstraction
 * to a System Under Learning (SUL), by allowing to pose {@link Query queries}: A query is a sequence
 * of input symbols (divided into a prefix and a suffix part, cf. {@link Query#getPrefix()}
 * and {@link Query#getSuffix()}, in reaction to which the SUL produces a specific observable
 * behavior (outputting a word, acceptance/rejection etc.).
 * 
 * @author Malte Isberner
 * @author Maik Merten
 * 
 * @see DefaultQuery
 */
@ParametersAreNonnullByDefault
public interface MembershipOracle<I, D> extends QueryAnswerer<I, D> {
	
	static interface DFAMembershipOracle<I> extends MembershipOracle<I,Boolean> {}
	static interface MealyMembershipOracle<I,O> extends MembershipOracle<I,Word<O>> {}

	/**
	 * Processes the specified collection of queries. When this method returns,
	 * each of the contained queries {@link Query#answer(Object)} method should have
	 * been called with an argument reflecting the SUL response to the respective query.
	 * 
	 * @param queries the queries to process
	 * @see Query#answer(Object)
	 */
	public void processQueries(Collection<? extends Query<I, D>> queries);
	
	
	/**
	 * Processes a single query. When this method returns, the {@link Query#answer(Object)}
	 * method of the supplied object will have been called with an argument reflecting
	 * the SUL response to the respective query.
	 * <p>
	 * The default implementation of this method will simply wrap the provided {@link Query}
	 * in a singleton {@link Collection} using {@link Collections#singleton(Object)}.
	 * Implementations in subclasses should override this method to circumvent the Collection
	 * object creation, if possible.
	 * 
	 * @param query the query to process
	 */
	default public void processQuery(Query<I,D> query) {
		processQueries(Collections.singleton(query));
	}
	
	@Override
	default public D answerQuery(Word<I> input) {
		return answerQuery(Word.epsilon(), input);
	}
	
	@Override
	default public D answerQuery(Word<I> prefix, Word<I> suffix) {
		DefaultQuery<I, D> query = new DefaultQuery<>(prefix, suffix);
		processQuery(query);
		return query.getOutput();
	}
	
	@Override
	default public MembershipOracle<I, D> asOracle() {
		return this;
	}
}
