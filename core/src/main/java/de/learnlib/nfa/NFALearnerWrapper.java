/* Copyright (C) 2015 TU Dortmund
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
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package de.learnlib.nfa;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.automatalib.automata.fsa.NFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.fsa.NFAs;
import net.automatalib.words.Alphabet;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.oracles.DefaultQuery;

@ParametersAreNonnullByDefault
public class NFALearnerWrapper<I> implements LearningAlgorithm.DFALearner<I> {
	@Nonnull
	private final Alphabet<I> alphabet;
	@Nonnull
	private final LearningAlgorithm<? extends NFA<?,I>,I,Boolean> nfaLearner;
	
	public NFALearnerWrapper(Alphabet<I> alphabet, LearningAlgorithm<? extends NFA<?,I>,I,Boolean> nfaLearner) {
		this.alphabet = alphabet;
		this.nfaLearner = nfaLearner;
	}

	@Override
	public void startLearning() {
		nfaLearner.startLearning();
	}

	@Override
	public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
		return nfaLearner.refineHypothesis(ceQuery);
	}

	@Override
	public CompactDFA<I> getHypothesisModel() {
		NFA<?,I> nfaHyp = nfaLearner.getHypothesisModel();
		CompactDFA<I> dfaHyp = NFAs.determinize(nfaHyp, alphabet);
		return dfaHyp;
	}
	
	@Override
	public String toString() {
		return nfaLearner.toString();
	}

}
