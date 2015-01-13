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
import net.automatalib.words.WordBuilder;

import com.github.misberner.buildergen.annotations.GenerateBuilder;

import de.learnlib.algorithms.ttt.base.BaseTTTLearner;
import de.learnlib.algorithms.ttt.base.DTNode;
import de.learnlib.algorithms.ttt.base.TTTState;
import de.learnlib.algorithms.ttt.base.TTTTransition;
import de.learnlib.api.MembershipOracle;
import de.learnlib.counterexamples.LocalSuffixFinder;

public class TTTLearnerMealy<I, O> extends
		BaseTTTLearner<MealyMachine<?, I, ?, O>, I, Word<O>> {

	@GenerateBuilder(defaults = BaseTTTLearner.BuilderDefaults.class)
	public TTTLearnerMealy(Alphabet<I> alphabet,
			MembershipOracle<I, Word<O>> oracle,
			LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder) {
		super(alphabet, oracle, new TTTHypothesisMealy<I,O>(alphabet), suffixFinder);
	}

	@Override
	@SuppressWarnings("unchecked")
	public MealyMachine<?, I, ?, O> getHypothesisModel() {
		return (TTTHypothesisMealy<I, O>) hypothesis;
	}
	
	@Override
	protected TTTTransition<I,Word<O>> createTransition(TTTState<I,Word<O>> state, I sym) {
		TTTTransitionMealy<I,O> trans = new TTTTransitionMealy<>(state, sym);
		trans.output = query(state, Word.fromLetter(sym)).firstSymbol();
		return trans;
	}

	@Override
	protected Word<O> predictSuccOutcome(TTTTransition<I, Word<O>> trans,
			DTNode<I, Word<O>> succSeparator) {
		TTTTransitionMealy<I, O> mtrans = (TTTTransitionMealy<I, O>) trans;
		if (succSeparator == null) {
			return Word.fromLetter(mtrans.output);
		}
		return succSeparator.subtreeLabel(trans.getDTTarget()).prepend(mtrans.output);
	}

	@Override
	protected Word<O> computeHypothesisOutput(TTTState<I, Word<O>> state,
			Iterable<? extends I> suffix) {
		TTTState<I,Word<O>> curr = state;
		
		WordBuilder<O> wb = new WordBuilder<>();
		
		for (I sym : suffix) {
			TTTTransitionMealy<I, O> trans = (TTTTransitionMealy<I,O>) hypothesis.getInternalTransition(curr, sym);
			wb.append(trans.output);
			curr = getTarget(trans);
		}
		
		return wb.toWord();
	}

}
