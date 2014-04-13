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
package de.learnlib.discriminationtree;

import java.util.HashMap;
import java.util.Map;


public class MultiDTNode<I, O, D> extends DTNode<I,O,D> {

	public MultiDTNode(D data) {
		super(data);
	}
	
	protected MultiDTNode(DTNode<I, O, D> parent, O parentOutcome, D data) {
		super(parent, parentOutcome, data);
	}

	@Override
	protected Map<O, DTNode<I, O, D>> createChildMap() {
		return new HashMap<>();
	}

	@Override
	protected DTNode<I, O, D> createChild(O outcome, D data) {
		return new MultiDTNode<>(this, outcome, data);
	}


}
