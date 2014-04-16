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
package de.learnlib.algorithms.discriminationtree.dfa;

import java.util.Collection;

import net.automatalib.automata.fsa.abstractimpl.AbstractDFA;
import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;

final class HypothesisWrapperDFA<I> extends
		AbstractDFA<HState<I, Boolean, Boolean, Void>, I> {
	
	private final DTLearnerHypothesis<I, Boolean, Boolean, Void> dtHypothesis;
	
	public HypothesisWrapperDFA(DTLearnerHypothesis<I,Boolean,Boolean,Void> dtHypothesis) {
		this.dtHypothesis = dtHypothesis;
	}

	@Override
	public Collection<HState<I, Boolean, Boolean, Void>> getStates() {
		return dtHypothesis.getStates();
	}

	@Override
	public HState<I, Boolean, Boolean, Void> getInitialState() {
		return dtHypothesis.getInitialState();
	}

	@Override
	public HState<I, Boolean, Boolean, Void> getTransition(
			HState<I, Boolean, Boolean, Void> state, I input) {
		return dtHypothesis.getSuccessor(state, input);
	}

	@Override
	public boolean isAccepting(HState<I, Boolean, Boolean, Void> state) {
		return state.getProperty().booleanValue();
	}


}
