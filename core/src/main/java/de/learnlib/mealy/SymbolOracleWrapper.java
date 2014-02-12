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
package de.learnlib.mealy;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;

import net.automatalib.words.Word;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Word-to-Symbol-Oracle adapter.
 * 
 * Wraps an oracle which uses {@link Word}s as its output to an oracle which only
 * yields the last symbol of each output.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
@ParametersAreNonnullByDefault
final class SymbolOracleWrapper<I, O> implements MembershipOracle<I, O> {
	
	@ParametersAreNonnullByDefault
	private static final class LastSymbolQuery<I,O> extends Query<I,Word<O>> {
		
		private final Query<I,O> originalQuery;
		
		public LastSymbolQuery(Query<I,O> originalQuery) {
			this.originalQuery = originalQuery;
		}

		@Override
		@Nonnull
		public Word<I> getPrefix() {
			return originalQuery.getPrefix();
		}

		@Override
		@Nonnull
		public Word<I> getSuffix() {
			return originalQuery.getSuffix();
		}

		@Override
		public void answer(Word<O> output) {
			if(output == null) {
				throw new IllegalArgumentException("Query answer words must not be null");
			}
			originalQuery.answer(output.isEmpty() ? null : output.lastSymbol());
		}
		
	}
	
	private final MembershipOracle<I,Word<O>> wordOracle;

	/**
	 * Constructor.
	 * @param wordOracle the {@link MembershipOracle} returning output words.
	 */
	public SymbolOracleWrapper(MembershipOracle<I,Word<O>> wordOracle) {
		this.wordOracle = wordOracle;
	}

	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, O>> queries) {
		List<LastSymbolQuery<I,O>> lsQueries = new ArrayList<LastSymbolQuery<I,O>>(queries.size());
		for(Query<I,O> qry : queries)
			lsQueries.add(new LastSymbolQuery<I,O>(qry));
		
		wordOracle.processQueries(lsQueries);
	}

}
