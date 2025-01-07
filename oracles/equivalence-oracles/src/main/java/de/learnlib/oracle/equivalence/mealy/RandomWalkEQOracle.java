/* Copyright (C) 2013-2025 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.oracle.equivalence.mealy;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.sul.SUL;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.common.util.collection.CollectionUtil;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a random walk over the hypothesis. A random walk restarts with a fixed probability after every step and
 * terminates after a fixed number of steps or with a counterexample. The number of steps to termination may be reset
 * for every new search.
 *
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 */
public class RandomWalkEQOracle<I, O> implements MealyEquivalenceOracle<I, O> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomWalkEQOracle.class);

    /**
     * probability to restart before step.
     */
    private final double restartProbability;

    /**
     * maximum number of steps.
     */
    private final long maxSteps;
    /**
     * RNG.
     */
    private final Random random;
    /**
     * System under learning.
     */
    private final SUL<I, O> sul;
    /**
     * step counter.
     */
    private long steps;
    /**
     * flag for resetting step count after every search.
     */
    private boolean resetStepCount;

    public RandomWalkEQOracle(SUL<I, O> sul,
                              double restartProbability,
                              long maxSteps,
                              boolean resetStepCount,
                              Random random) {
        this(sul, restartProbability, maxSteps, random);
        this.resetStepCount = resetStepCount;
    }

    public RandomWalkEQOracle(SUL<I, O> sul, double restartProbability, long maxSteps, Random random) {
        this.restartProbability = restartProbability;
        this.maxSteps = maxSteps;
        this.random = random;
        this.sul = sul;
    }

    @Override
    public @Nullable DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                                 Collection<? extends I> inputs) {
        return doFindCounterExample(hypothesis, inputs);
    }

    private <S, T> @Nullable DefaultQuery<I, Word<O>> doFindCounterExample(MealyMachine<S, I, T, O> hypothesis,
                                                                           Collection<? extends I> inputs) {
        // reset termination counter?
        if (resetStepCount) {
            steps = 0;
        }

        if (inputs.isEmpty()) {
            LOGGER.warn(Category.COUNTEREXAMPLE,
                        "Passed empty set of inputs to equivalence oracle; no counterexample can be found!");
            return null;
        }

        List<? extends I> choices = CollectionUtil.randomAccessList(inputs);
        int bound = choices.size();
        S cur = hypothesis.getInitialState();
        WordBuilder<I> wbIn = new WordBuilder<>();
        WordBuilder<O> wbOut = new WordBuilder<>();

        boolean first = true;
        sul.pre();
        try {
            while (steps < maxSteps) {

                if (first) {
                    first = false;
                } else {
                    // restart?
                    double restart = random.nextDouble();
                    if (restart < restartProbability) {
                        sul.post();
                        sul.pre();
                        cur = hypothesis.getInitialState();
                        wbIn.clear();
                        wbOut.clear();
                        first = true;
                    }
                }

                // step
                steps++;
                I in = choices.get(random.nextInt(bound));
                O outSul;

                outSul = sul.step(in);

                assert cur != null;
                O outHyp = hypothesis.getTransitionProperty(cur, in);
                wbIn.add(in);
                wbOut.add(outSul);

                // ce?
                if (!Objects.equals(outSul, outHyp)) {
                    DefaultQuery<I, Word<O>> ce = new DefaultQuery<>(wbIn.toWord());
                    ce.answer(wbOut.toWord());
                    return ce;
                }
                cur = hypothesis.getSuccessor(cur, in);
            }
            return null;
        } finally {
            sul.post();
        }
    }
}
