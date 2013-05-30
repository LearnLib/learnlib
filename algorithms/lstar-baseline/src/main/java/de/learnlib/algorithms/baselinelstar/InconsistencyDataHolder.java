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
package de.learnlib.algorithms.baselinelstar;

import net.automatalib.words.Word;

public class InconsistencyDataHolder<I> {

	private final Word<I> firstState;
	private final Word<I> secondState;
	private final I differingSymbol;

	public InconsistencyDataHolder(Word<I> firstState, Word<I> secondState, I differingSymbol) {
		this.firstState = firstState;
		this.secondState = secondState;
		this.differingSymbol = differingSymbol;
	}

	public Word<I> getFirstState() {
		return firstState;
	}

	public Word<I> getSecondState() {
		return secondState;
	}

	public I getDifferingSymbol() {
		return differingSymbol;
	}
}
