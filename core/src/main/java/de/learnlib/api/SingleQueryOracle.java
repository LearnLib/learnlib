/* Copyright (C) 2015 TU Dortmund
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

import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.oracles.MQUtil;

/**
 * Base interface for oracles whose semantic is defined in terms of directly answering single queries
 * (like a {@link QueryAnswerer}, and that cannot profit from batch processing of queries.
 * <p>
 * Implementing this class instead of directly implementing {@link MembershipOracle} means that
 * the {@link #answerQuery(Word, Word)} instead of the {@link #processQueries(Collection)} method
 * needs to be implemented.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@ParametersAreNonnullByDefault
public interface SingleQueryOracle<I, D> extends MembershipOracle<I, D> {
	
	public static interface SingleQueryOracleDFA<I>
		extends SingleQueryOracle<I, Boolean>, DFAMembershipOracle<I> {}
	public static interface SingleQueryOracleMealy<I,O>
		extends SingleQueryOracle<I, Word<O>>, MealyMembershipOracle<I, O> {}
	
	@Override
	default public void processQueries(Collection<? extends Query<I, D>> queries) {
		MQUtil.answerQueriesAuto(this, queries);
	}
	
	@Override
	default public void processQuery(Query<I,D> query) {
		D output = answerQuery(query.getPrefix(), query.getSuffix());
		query.answer(output);
	}
	
	@Override
	public abstract D answerQuery(Word<I> prefix, Word<I> suffix);

}
