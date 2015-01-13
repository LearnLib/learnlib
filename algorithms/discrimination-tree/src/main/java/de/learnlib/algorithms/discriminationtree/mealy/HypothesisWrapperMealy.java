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
package de.learnlib.algorithms.discriminationtree.mealy;

import java.util.Collection;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.algorithms.discriminationtree.hypothesis.DTLearnerHypothesis;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;

final class HypothesisWrapperMealy<I, O> implements MealyMachine<HState<I,Word<O>,Void,O>, I, HTransition<I,Word<O>,Void,O>, O> {

	private final DTLearnerHypothesis<I, Word<O>, Void, O> dtHypothesis;
	
	public HypothesisWrapperMealy(DTLearnerHypothesis<I,Word<O>,Void,O> dtHypothesis) {
		this.dtHypothesis = dtHypothesis;
	}
	
	@Override
	public HState<I, Word<O>, Void, O> getSuccessor(
			HTransition<I, Word<O>, Void, O> trans) {
		return dtHypothesis.getSuccessor(trans);
	}

	@Override
	public Collection<HState<I, Word<O>, Void, O>> getStates() {
		return dtHypothesis.getStates();
	}

	@Override
	public HState<I, Word<O>, Void, O> getInitialState() {
		return dtHypothesis.getInitialState();
	}

	@Override
	public HTransition<I, Word<O>, Void, O> getTransition(
			HState<I, Word<O>, Void, O> state, I input) {
		return dtHypothesis.getTransition(state, input);
	}
	
	@Override
	public O getTransitionOutput(HTransition<I, Word<O>, Void, O> trans) {
		return trans.getProperty();
	}

}
