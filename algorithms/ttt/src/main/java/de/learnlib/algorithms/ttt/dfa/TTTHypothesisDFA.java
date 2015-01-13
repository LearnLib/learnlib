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
package de.learnlib.algorithms.ttt.dfa;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.words.Alphabet;
import de.learnlib.algorithms.ttt.base.TTTHypothesis;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;

public class TTTHypothesisDFA<I> extends TTTHypothesis<I, Boolean, TTTState<I,Boolean>> 
		implements DFA<TTTState<I,Boolean>,I> {

	public TTTHypothesisDFA(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	public TTTState<I, Boolean> getSuccessor(TTTState<I, Boolean> transition) {
		return transition;
	}

	@Override
	protected TTTState<I, Boolean> mapTransition(
			TTTTransition<I, Boolean> internalTransition) {
		return internalTransition.getTarget();
	}

	@Override
	protected TTTState<I, Boolean> newState(int alphabetSize,
			TTTTransition<I, Boolean> parent, int id) {
		return new TTTStateDFA<>(alphabet.size(), parent, id);
	}

	@Override
	public boolean isAccepting(TTTState<I, Boolean> state) {
		return ((TTTStateDFA<I>) state).accepting;
	}
}
