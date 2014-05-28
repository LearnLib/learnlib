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
package de.learnlib.cache.dfa;

import de.learnlib.api.Query;
import net.automatalib.words.Word;

/**
 * Proxy query. Answers an underlying query, and also
 * stores the result.
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol class
 */
final class ProxyQuery<I> extends Query<I,Boolean> {
	private final Query<I,Boolean> origQuery;
	private Boolean answer;
	
	/**
	 * Constructor.
	 * @param origQuery the original query to forward the answer to
	 */
	public ProxyQuery(Query<I,Boolean> origQuery) {
		this.origQuery = origQuery;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.Query#getPrefix()
	 */
	@Override
	public Word<I> getPrefix() {
		return origQuery.getPrefix();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.Query#getSuffix()
	 */
	@Override
	public Word<I> getSuffix() {
		return origQuery.getSuffix();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.learnlib.api.Query#answer(java.lang.Object)
	 */
	@Override
	public void answer(Boolean output) {
		origQuery.answer(output);
		this.answer = output;
	}
	
	/**
	 * Retrieves the answer that this oracle received. 
	 * @return the answer that was received
	 */
	public Boolean getAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		return origQuery.toString();
	}

}

