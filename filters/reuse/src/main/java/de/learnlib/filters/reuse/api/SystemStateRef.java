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
package de.learnlib.filters.reuse.api;

import net.automatalib.words.Word;

/**
 * A reference to a system state that can be represented by an arbitrary
 * type <i>M</i>. e.g. an java object, a map of key value pairs, or something
 * completely different.
 * 
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 *
 * @param <M> The type of the system state.
 * @param <I> The type of the input alphabet.
 * @param <O> The type of the output alphabet.
 */
public class SystemStateRef<M, I, O> {
	private Word<I> prefixInput;
	private Word<O> prefixOutput;
	private M systemState;

	public Word<I> getPrefixInput() {
		return prefixInput;
	}

	public void setPrefixInput(Word<I> prefixInput) {
		this.prefixInput = prefixInput;
	}

	public Word<O> getPrefixOutput() {
		return prefixOutput;
	}

	public void setPrefixOutput(Word<O> prefixOutput) {
		this.prefixOutput = prefixOutput;
	}

	public M getSystemState() {
		return systemState;
	}

	public void setSystemState(M systemState) {
		this.systemState = systemState;
	}
}
