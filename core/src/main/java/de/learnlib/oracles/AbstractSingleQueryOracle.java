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

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.QueryAnswerer;

import net.automatalib.words.Word;

@ParametersAreNonnullByDefault
public abstract class AbstractSingleQueryOracle<I, D> implements MembershipOracle<I, D>, QueryAnswerer<I, D> {
	
	public static abstract class AbstractSingleQueryOracleDFA<I> extends AbstractSingleQueryOracle<I,Boolean> implements DFAMembershipOracle<I> {
	}
	
	public static abstract class AbstractSingleQueryOracleMealy<I,O> extends AbstractSingleQueryOracle<I,Word<O>> implements MealyMembershipOracle<I,O> {
	}

	public AbstractSingleQueryOracle() {
	}

	/* (non-Javadoc)
	 * @see de.learnlib.api.MembershipOracle#processQueries(java.util.Collection)
	 */
	@Override
	public void processQueries(Collection<? extends Query<I, D>> queries) {
		MQUtil.answerQueries(this, queries);
	}
	
}
