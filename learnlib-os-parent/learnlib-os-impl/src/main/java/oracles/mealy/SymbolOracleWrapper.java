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
package oracles.mealy;

import java.util.Collection;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;


/**
 * Word-to-Symbol-Oracle adapter.
 * 
 * Wraps an oracle which uses {@link Word}s as its output to an oracle which only
 * yields the last symbol of each output.
 * 
 * CAVEAT: This class employs extremely evil black generics magic. It works fine for the
 * standard {@link Query} class, but might fail for any more specific subclasses.
 * 
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <I> input symbol class
 * @param <O> output symbol class
 */
public class SymbolOracleWrapper<I, O> implements MembershipOracle<I, O> {
	
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
	@SuppressWarnings("unchecked")
	public void processQueries(Collection<Query<I, O>> queries) {
		// FIXME might fail if we allow subclassing Query
		// I am so going to hell for this, but it's the most efficient way
		Collection<Query<I,Word<O>>> wordQueries = (Collection<Query<I,Word<O>>>)(Collection<?>)queries;
		wordOracle.processQueries(wordQueries);
		
		for(Query<I,Word<O>> wordQuery : wordQueries) {
			Word<O> outWord = wordQuery.getOutput();
			O outSym = outWord.getSymbol(outWord.size() - 1);
			Query<I,O> symQuery = (Query<I,O>)(Query<?,?>)wordQuery;
			symQuery.setOutput(outSym);
		}
	}

}
