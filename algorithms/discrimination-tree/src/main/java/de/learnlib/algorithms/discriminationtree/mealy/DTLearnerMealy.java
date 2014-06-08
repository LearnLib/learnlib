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

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.discriminationtree.AbstractDTLearner;
import de.learnlib.algorithms.discriminationtree.hypothesis.HState;
import de.learnlib.algorithms.discriminationtree.hypothesis.HTransition;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.counterexamples.LocalSuffixFinder;
import de.learnlib.discriminationtree.MultiDTree;
import de.learnlib.oracles.AbstractQuery;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

/**
 * 
 * @author Malte Isberner 
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 */
public class DTLearnerMealy<I, O>
		extends
		AbstractDTLearner<MealyMachine<?, I, ?, O>, I, Word<O>, Void, O>
		implements
		MealyLearner<I, O> {
	
	private final HypothesisWrapperMealy<I, O> hypWrapper;

	/**
	 * Constructor.
	 * @param alphabet the input alphabet
	 * @param oracle the membership oracle
	 * @param suffixFinder method to use for analyzing counterexamples
	 */
	@GenerateBuilder(defaults = AbstractDTLearner.BuilderDefaults.class)
	public DTLearnerMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder,
			boolean repeatedCounterexampleEvaluation) {
		super(alphabet, oracle, suffixFinder, repeatedCounterexampleEvaluation, new MultiDTree<I,Word<O>,HState<I,Word<O>,Void,O>>(oracle));
		this.hypWrapper = new HypothesisWrapperMealy<>(hypothesis);
	}

	@Override
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		return hypWrapper;
	}

	@Override
	protected Query<I, Word<O>> spQuery(HState<I, Word<O>, Void, O> state) {
		return null;
	}

	@Override
	protected Query<I, Word<O>> tpQuery(
			final HTransition<I, Word<O>, Void, O> transition) {
		return new AbstractQuery<I,Word<O>>(transition.getSource().getAccessSequence(), Word.fromLetter(transition.getSymbol())) {
			@Override
			public void answer(Word<O> output) {
				transition.setProperty(output.firstSymbol());
			}
		};
	}
}
