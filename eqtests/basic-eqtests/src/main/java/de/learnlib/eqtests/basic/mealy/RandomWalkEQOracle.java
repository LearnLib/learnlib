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
package de.learnlib.eqtests.basic.mealy;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;

/**
 * Performs a random walk over the hypothesis. A random walk restarts with a
 * fixed probability after every step and terminates after a fixed number of
 * steps or with a counterexample. The number of steps to termination may be
 * reset for every new search.
 * 
 * @param <A>
 *            hypothesis format
 * @param <I>
 *            input symbols class
 * @param <O>
 *            output symbol class
 * 
 * @author falkhowar
 */
public class RandomWalkEQOracle<I, O>
		implements MealyEquivalenceOracle<I,O> {

	/**
	 * probability to restart before step.
	 */
	private final double restartProbability;

	/**
	 * maximum number of steps.
	 */
	private final long maxSteps;

	/**
	 * step counter.
	 */
	private long steps = 0;

	/**
	 * flag for reseting step count after every search.
	 */
	private boolean resetStepCount;

	/**
	 * RNG.
	 */
	private final Random random;

	/**
	 * System under learning.
	 */
	private final SUL<I, O> sul;

	/**
	 * Constructor.
	 * 
	 * @param restartProbability
	 * @param maxSteps
	 * @param random
	 * @param sul
	 */
	public RandomWalkEQOracle(double restartProbability, long maxSteps,
			Random random, SUL<I, O> sul) {
		this.restartProbability = restartProbability;
		this.maxSteps = maxSteps;
		this.random = random;
		this.sul = sul;
	}

	public RandomWalkEQOracle(double restartProbability, long maxSteps,
			boolean resetStepCount, Random random, SUL<I, O> sul) {
		this(restartProbability, maxSteps, random, sul);
		this.resetStepCount = resetStepCount;
	}

	/**
	 * 
	 * @param hypothesis
	 * @param inputs
	 * @return null or a counterexample
	 */
	@Override
	public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?,I,?,O> hypothesis,
			Collection<? extends I> inputs) {
		return doFindCounterExample(hypothesis, inputs);
	}

	private <S, T> DefaultQuery<I, Word<O>> doFindCounterExample(
			MealyMachine<S, I, T, O> hypothesis, Collection<? extends I> inputs) {
		// reset termination counter?
		if (resetStepCount) {
			steps = 0;
		}

		List<? extends I> choices = CollectionsUtil.randomAccessList(inputs);
		int bound = choices.size();
		S cur = hypothesis.getInitialState();
		WordBuilder<I> wbIn = new WordBuilder<>();
		WordBuilder<O> wbOut = new WordBuilder<>();

                boolean first = true;
                sul.pre();
		while (steps < maxSteps) {

			// restart?
			double restart = random.nextDouble();
			if (restart < restartProbability) {
				if (first) {
                                    first = false;
                                }
                                else {
                                    sul.post();
                                }
                                sul.pre();
				cur = hypothesis.getInitialState();
				wbIn.clear();
				wbOut.clear();
			}

			// step
			steps++;
			I in = choices.get(random.nextInt(bound));
			O outSul = sul.step(in);
			T hypTrans = hypothesis.getTransition(cur, in);
			O outHyp = hypothesis.getTransitionOutput(hypTrans);
			wbIn.add(in);
			wbOut.add(outSul);

			// ce?
			if (!outSul.equals(outHyp)) {
				DefaultQuery<I, Word<O>> ce = new DefaultQuery<>(wbIn.toWord());
				ce.answer(wbOut.toWord());
                                sul.post();
				return ce;
			}
			cur = hypothesis.getSuccessor(cur, in);
		}
                sul.post();
		return null;
	}
}
