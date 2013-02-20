/* Copyright (C) 2012 TU Dortmund
   This file is part of LearnLib 

   LearnLib is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License version 3.0 as published by the Free Software Foundation.

   LearnLib is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with LearnLib; if not, see
   <http://www.gnu.de/documents/lgpl.en.html>
 */

package de.learnlib.api;

import java.util.List;

/**
 *
 * @author merten
 */
public class Query<I, O> {

	public final List<I> toState, future;
	public O output;

	public Query(List<I> toState, List<I> future) {
		this.toState = toState;
		this.future = future;
	}
}