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

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.SUL;
import de.learnlib.oracles.DefaultQuery;
import java.util.Collection;
import java.util.Random;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Performs a random walk over the hypothesis.
 * A random walk restarts with a fixed probability after every step 
 * and terminates after a fixed number of steps or with a counterexample.
 * The number of steps to termination may be reset for every new search.
 *
 * @param <A> hypothesis format
 * @param <I> input symbols class
 * @param <O> output symbol class
 *
 * @author falkhowar
 */
public class RandomWalkEQOracle<A extends MealyMachine<Object, I, ?, O>, I, O> 
	implements EquivalenceOracle<A, I, Word<O>> {

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
    public RandomWalkEQOracle(double restartProbability, long maxSteps, Random random, SUL<I, O> sul) {
	this.restartProbability = restartProbability;
	this.maxSteps = maxSteps;
	this.random = random;
	this.sul = sul;
    }

    public RandomWalkEQOracle(double restartProbability, long maxSteps, boolean resetStepCount, Random random, SUL<I, O> sul) {
	this(restartProbability,maxSteps,random,sul);
	this.resetStepCount = resetStepCount;
    }


    /**
     * 
     * @param hypothesis
     * @param inputs
     * @return null or a counterexample 
     */
    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(A hypothesis, Collection<? extends I> inputs) {

	// reset termination counter?
	if (resetStepCount) {
	    steps = 0;
	}

	@SuppressWarnings({"unchecked"})
	I[] choices = (I[])inputs.toArray();
	int bound = choices.length;
	Object cur = hypothesis.getInitialState();
	WordBuilder<I> wbIn = new WordBuilder<>();
	WordBuilder<O> wbOut = new WordBuilder<>();

	while (steps < maxSteps) {

	    // restart?
	    double restart = random.nextDouble();
	    if (restart > restartProbability || restartProbability == 1.0) {
		sul.reset();
		cur = hypothesis.getInitialState();
		wbIn.clear();
		wbOut.clear();
	    }

	    // step
	    steps++;
	    I in = choices[random.nextInt(bound)];
	    O outSul = sul.step(in);
	    O outHyp = hypothesis.getOutput(cur, in);
	    wbIn.add(in);
	    wbOut.add(outSul);

	    // ce?
	    if (!outSul.equals(outHyp)) {
		DefaultQuery<I, Word<O>> ce = new DefaultQuery<>(wbIn.toWord());
		ce.answer(wbOut.toWord());
		return ce;
	    }
	    cur = hypothesis.getSuccessor(cur, in);
	}

	return null;
    }
}
