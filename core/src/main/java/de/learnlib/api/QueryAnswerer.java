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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import de.learnlib.oracles.QueryAnswererOracle;

/**
 * 
 * @author Malte Isberner
 *
 * @param <I> input symbol type
 * @param <D> output domain type
 */
@ParametersAreNonnullByDefault
public interface QueryAnswerer<I, D> {
	@Nullable
	default public D answerQuery(Word<I> input) {
		return answerQuery(Word.epsilon(), input);
	}
	
	@Nullable
	public D answerQuery(Word<I> prefix, Word<I> suffix);
	
	
	@Nonnull
	default public MembershipOracle<I, D> asOracle() {
		return new QueryAnswererOracle<>(this);
	}
}
