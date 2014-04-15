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

import java.util.Map;

public class BinaryDTNode<I, D> extends DTNode<I, Boolean, D> {

	public BinaryDTNode(D data) {
		super(data);
	}

	protected BinaryDTNode(DTNode<I, Boolean, D> parent, Boolean parentOutcome,
			D data) {
		super(parent, parentOutcome, data);
	}

	@Override
	protected Map<Boolean, DTNode<I, Boolean, D>> createChildMap() {
		return new BooleanMap<>();
	}

	@Override
	protected DTNode<I, Boolean, D> createChild(Boolean outcome, D data) {
		return new BinaryDTNode<>(this, outcome, data);
	}

}
