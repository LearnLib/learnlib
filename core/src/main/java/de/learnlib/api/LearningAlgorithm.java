/* Copyright (C) 2013-2014 TU Dortmund
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
package de.learnlib.api;

import javax.annotation.Nonnull;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import de.learnlib.oracles.DefaultQuery;



/**
 * Basic interface for a model inference algorithm.
 * <p>
 * Actively inferring models (such as DFAs or Mealy machines) consists of the construction
 * of an initial hypothesis, which is subsequently refined using counterexamples
 * (see {@link EquivalenceOracle}). 
 * 
 * @author Maik Merten <maikmerten@googlemail.com>
 * @author Malte Isberner <malte.isberner@gmail.com>
 *
 * @param <M> model class.
 * @param <I> input symbol class.
 * @param <O> output class.
 */
public interface LearningAlgorithm<M, I, O> {
	
	public static interface DFALearner<I> extends LearningAlgorithm<DFA<?,I>,I,Boolean> {}
	public static interface MealyLearner<I,O> extends LearningAlgorithm<MealyMachine<?,I,?,O>,I,Word<O>> {}
	
	/**
	 * Starts the model inference process, creating an initial hypothesis in the provided
	 * model object. Please note that it should be illegal to invoke this method twice.
	 */
	public void startLearning();
	
	/**
	 * Triggers a refinement of the model by providing a counterexample.
	 * A counterexample is a query which exposes different behavior of the real SUL compared
	 * to the hypothesis. Please note that invoking this method before an initial
	 * invocation of {@link #startLearning()} should be illegal.
	 * 
	 * @param ceQuery the query which exposes diverging behavior, as posed to the real SUL
	 * (i.e. with the SULs output).
	 * @return <tt>true</tt> if the counterexample triggered a refinement of the hypothesis,
	 * <tt>false</tt> otherwise (i.e., it was no counterexample).
	 */
	public boolean refineHypothesis(@Nonnull DefaultQuery<I, O> ceQuery);
	
	/**
	 * Returns the current hypothesis model.
	 * <p>
	 * N.B.: By the contract of this interface, the model returned by this method may not be
	 * modified (i.e., M generally should refer to an immutable interface), and its validity
	 * is retained only until the next invocation of {@link #refineHypothesis(DefaultQuery)}. If
	 * older hypotheses have to be maintained, a copy of the returned model must be made.
	 * <p>
	 * Please note that it should be illegal to invoke this method before an initial invocation
	 * of {@link #startLearning()}.
	 * @return the current hypothesis model.
	 */
	@Nonnull
	public M getHypothesisModel();
}
