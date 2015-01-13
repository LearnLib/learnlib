/* Copyright (C) 2014 TU Dortmund
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
package de.learnlib.mapper;

import de.learnlib.api.SUL;
import de.learnlib.mapper.api.Mapper;

/**
 * Utility methods for manipulating mappers.
 * 
 * @author Malte Isberner
 *
 */
public abstract class Mappers {

	public static <AI,AO,ACI,CAO,CI,CO>
	Mapper<AI,AO,CI,CO> compose(Mapper<? super AI,? extends AO,ACI,CAO> outerMapper,
			Mapper<? super ACI,? extends CAO,? extends CI,? super CO> innerMapper) {
		return new MapperComposition<>(outerMapper, innerMapper);
	}
	
	public static <AI,AO,CI,CO>
	SUL<AI,AO> apply(Mapper<? super AI,? extends AO,CI,CO> mapper, SUL<? super CI,? extends CO> sul) {
		return new MappedSUL<>(mapper, sul);
	}
	
	private Mappers() {
		throw new AssertionError("Constructor should not be invoked");
	}

}
