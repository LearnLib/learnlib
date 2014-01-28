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
package de.learnlib.oracles;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.QueryAnswerer;

@ParametersAreNonnullByDefault
public abstract class MQUtil {
	
	@Nullable
	public static <I,O> O output(MembershipOracle<I,O> oracle, Word<I> queryWord) {
		return query(oracle, queryWord).getOutput();
	}
	
	@Nullable
	public static <I,O> O output(MembershipOracle<I,O> oracle, Word<I> prefix, Word<I> suffix) {
		return query(oracle, prefix, suffix).getOutput();
	}
	
	@Nonnull
	public static <I,O> DefaultQuery<I,O> query(MembershipOracle<I,O> oracle, Word<I> prefix, Word<I> suffix) {
		DefaultQuery<I,O> qry = new DefaultQuery<>(prefix, suffix);
		oracle.processQueries(Collections.singleton(qry));
		return qry;
	}
	
	@Nonnull
	public static <I,O> DefaultQuery<I,O> query(MembershipOracle<I,O> oracle, Word<I> queryWord) {
		return query(oracle, Word.<I>epsilon(), queryWord);
	}
	
	public static <I,O> void answerQueries(QueryAnswerer<I,O> answerer, Collection<? extends Query<I,O>> queries) {
		for(Query<I,O> query : queries) {
			Word<I> prefix = query.getPrefix();
			Word<I> suffix = query.getSuffix();
			O answer = answerer.answerQuery(prefix, suffix);
			query.answer(answer);
		}
	}
}
