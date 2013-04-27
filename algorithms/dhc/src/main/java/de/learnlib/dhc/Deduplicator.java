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

package de.learnlib.dhc;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that deduplicates Objects regarding their equals
 * function.
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 */
public class Deduplicator<C> {
	
	private List<C> cache = new ArrayList<>();
	
	/**
	 * Find an equal representative object for the provided object.
	 * @param instance object which should be deduplicated
	 * @return equal representative object or input object
	 */
	public C deduplicate(C instance) {
		int idx = cache.indexOf(instance);
		if(idx == -1) {
			cache.add(instance);
			return instance;
		} else {
			return cache.get(idx);
		}
	}
	
	
}
