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
package de.learnlib.algorithms.ttt.mealy;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.algorithms.ttt.base.TTTHypothesis;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;

public class TTTHypothesisMealy<I, O> extends
		TTTHypothesis<I, Word<O>, TTTTransitionMealy<I, O>>
		implements MealyMachine<TTTState<I,Word<O>>, I, TTTTransitionMealy<I,O>, O> {

	public TTTHypothesisMealy(Alphabet<I> alphabet) {
		super(alphabet);
	}

	@Override
	public TTTState<I, Word<O>> getSuccessor(TTTTransitionMealy<I, O> transition) {
		return transition.getTarget();
	}

	@Override
	protected TTTTransitionMealy<I, O> mapTransition(
			TTTTransition<I, Word<O>> internalTransition) {
		return (TTTTransitionMealy<I,O>) internalTransition;
	}

	@Override
	public O getTransitionOutput(TTTTransitionMealy<I, O> transition) {
		return transition.getOutput();
	}


}
