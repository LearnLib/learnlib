/* Copyright (C) 2014 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
package de.learnlib.testsupport.queries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.automatalib.words.Word;
import de.learnlib.api.Query;

public abstract class TestQueries {

	
	
	public static <I, D>
	Collection<? extends Query<I,D>> createNoopQueries(int numQueries) {
		List<Query<I,D>> result = new ArrayList<>(numQueries);
		for(int i = 0; i < numQueries; i++) {
			result.add(new NoopQuery<I,D>(Word.<I>epsilon()));
		}
		return result;
	}
	
	
	private TestQueries() {
		throw new AssertionError("Constructor should not be invoked");
	}

}
