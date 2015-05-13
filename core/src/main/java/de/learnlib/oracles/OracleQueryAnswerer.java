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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.oracles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.QueryAnswerer;


/**
 * A {@link QueryAnswerer} that queries a {@link MembershipOracle} for implementing
 * the {@link #answerQuery(Word,Word)} method.
 * 
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 * 
 * @deprecated since 2015-05-10. {@link MembershipOracle} now extends {@link QueryAnswerer},
 * rendering this class obsolete.
 */
@Deprecated
@ParametersAreNonnullByDefault
public final class OracleQueryAnswerer<I, D> implements QueryAnswerer<I,D> {
	
	private final MembershipOracle<I,D> oracle;
	
	public OracleQueryAnswerer(MembershipOracle<I,D> oracle) {
		this.oracle = oracle;
	}

	@Override
	@Nullable
	public D answerQuery(Word<I> prefix, Word<I> suffix) {
		return oracle.answerQuery(prefix, suffix);
	}

}
