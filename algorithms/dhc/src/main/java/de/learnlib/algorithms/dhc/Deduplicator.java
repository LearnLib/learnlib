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

package de.learnlib.algorithms.dhc;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that deduplicates Objects regarding their equals
 * function.
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class Deduplicator<C> {
	
	private final Map<C,C> cache = new HashMap<>();
	
	/**
	 * Find an equal representative object for the provided object.
	 * @param instance object which should be deduplicated
	 * @return equal representative object or input object
	 */
	public C deduplicate(C instance) {
		C cached = cache.get(instance);
		if(cached != null)
			return cached;
		cache.put(instance, instance);
		return instance;
	}
	
	
}
