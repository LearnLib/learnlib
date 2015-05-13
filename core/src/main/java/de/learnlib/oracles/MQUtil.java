/* Copyright (C) 2013-2015 TU Dortmund
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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.QueryAnswerer;
import de.learnlib.settings.LearnLibSettings;

@ParametersAreNonnullByDefault
public abstract class MQUtil {

	public static int PARALLEL_THRESHOLD = -1;
	
	
	static {
		LearnLibSettings settings = LearnLibSettings.getInstance();
		PARALLEL_THRESHOLD = settings.getInt("queries.parallel.threshold", -1);
	}

	@Deprecated
	@Nullable
	public static <I,D> D output(MembershipOracle<I,D> oracle, Word<I> queryWord) {
		return oracle.answerQuery(queryWord);
	}
	
	@Deprecated
	@Nullable
	public static <I,D> D output(MembershipOracle<I,D> oracle, Word<I> prefix, Word<I> suffix) {
		return oracle.answerQuery(prefix, suffix);
	}
	
	@Nonnull
	public static <I,D> DefaultQuery<I,D> query(MembershipOracle<I,D> oracle, Word<I> prefix, Word<I> suffix) {
		DefaultQuery<I,D> qry = new DefaultQuery<>(prefix, suffix);
		oracle.processQuery(qry);
		return qry;
	}
	
	public static <I,D> DefaultQuery<I,D> normalize(MembershipOracle<I, D> oracle, DefaultQuery<I, D> query) {
		if (query.isNormalized()) {
			return query;
		}
		return query(oracle, Word.epsilon(), query.getInput());
	}
	
	@Nonnull
	public static <I,D> DefaultQuery<I,D> query(MembershipOracle<I,D> oracle, Word<I> queryWord) {
		return query(oracle, Word.<I>epsilon(), queryWord);
	}
	
	public static <I,D> void answerQueries(QueryAnswerer<I,D> answerer, Collection<? extends Query<I,D>> queries) {
		for(Query<I,D> query : queries) {
			Word<I> prefix = query.getPrefix();
			Word<I> suffix = query.getSuffix();
			D answer = answerer.answerQuery(prefix, suffix);
			query.answer(answer);
		}
	}
	
	public static <I,D> void answerQueriesParallel(QueryAnswerer<I,D> answerer, Collection<? extends Query<I,D>> queries) {
		queries.parallelStream().forEach(q -> {
			Word<I> prefix = q.getPrefix();
			Word<I> suffix = q.getSuffix();
			D answer = answerer.answerQuery(prefix, suffix);
			q.answer(answer);
		});
	}
	
	public static <I,D> void answerQueriesAuto(QueryAnswerer<I, D> answerer, Collection<? extends Query<I,D>> queries) {
		if (PARALLEL_THRESHOLD < 0 || queries.size() < PARALLEL_THRESHOLD) {
			answerQueries(answerer, queries);
		}
		else {
			answerQueriesParallel(answerer, queries);
		}
	}
	
	public static <I,D> boolean isCounterexample(DefaultQuery<I, D> query, SuffixOutput<I, D> hyp) {
		D qryOut = query.getOutput();
		D hypOut = hyp.computeSuffixOutput(query.getPrefix(), query.getSuffix());
		return !Objects.equals(qryOut, hypOut);
	}
}
