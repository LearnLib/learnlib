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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.SUL;

/**
 * A wrapper around a system under learning (SUL).
 * 
 * @author falkhowar
 */
@ParametersAreNonnullByDefault
public class SULOracle<I, O> extends AbstractSingleQueryOracle<I, Word<O>> implements MealyMembershipOracle<I,O> {

	private final SUL<I, O> sul;

	public SULOracle(SUL<I, O> sul) {
		this.sul = sul;
	}

	@Override
	@Nonnull
	public Word<O> answerQuery(Word<I> prefix, Word<I> suffix) {
		sul.pre();
		// Prefix: Execute symbols, don't record output
		for(I sym : prefix) {
			sul.step(sym);
		}
		
		// Suffix: Execute symbols, outputs constitute output word
		WordBuilder<O> wb = new WordBuilder<>(suffix.length());
		for(I sym : suffix) {
			wb.add(sul.step(sym));
		}
		
        sul.post();
		return wb.toWord();
	}

}
