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
package de.learnlib.testsupport.it.learner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.LearningExample;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.Output;
import net.automatalib.common.util.random.RandomUtil;
import net.automatalib.word.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

abstract class AbstractLearnerVariantITCase<I, D, M extends FiniteRepresentation & Output<I, D>> implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLearnerVariantITCase.class);

    private static final long NANOS_PER_MILLISECOND = 1_000_000;
    private static final long MILLIS_PER_SECOND = 1_000;

    private final LearnerVariant<? extends M, I, D> variant;
    private final LearningExample<I, ? extends M> example;
    private final EquivalenceOracle<? super M, I, D> eqOracle;

    AbstractLearnerVariantITCase(LearnerVariant<? extends M, I, D> variant,
                                 LearningExample<I, ? extends M> example,
                                 EquivalenceOracle<? super M, I, D> eqOracle) {
        this.variant = variant;
        this.example = example;
        this.eqOracle = eqOracle;
    }

    @Test
    public void testLearning() {
        LearningAlgorithm<? extends M, I, D> learner = variant.getLearner();

        Alphabet<I> alphabet = example.getAlphabet();
        M reference = example.getReferenceAutomaton();

        int maxRounds = variant.getMaxRounds();
        if (maxRounds < 0) {
            maxRounds = reference.size();
        }

        long start = System.nanoTime();

        learner.startLearning();

        int roundCounter = 0;
        DefaultQuery<I, D> ceQuery;

        while ((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
            roundCounter++;
            if (roundCounter > maxRounds) {
                Assert.fail("Learning took too many rounds (> " + maxRounds + ")");
            }

            boolean refined = learner.refineHypothesis(ceQuery);
            Assert.assertTrue(refined, "Real counterexample " + ceQuery.getInput() + " did not refine hypothesis");
        }

        M hypothesis = learner.getHypothesisModel();
        Assert.assertEquals(hypothesis.size(), reference.size());
        Assert.assertNull(checkEquivalence(hypothesis), "Final hypothesis does not match reference automaton");

        final List<I> trace = RandomUtil.sample(new Random(42), new ArrayList<>(alphabet), 5);
        final D output = reference.computeOutput(trace);

        Assert.assertFalse(learner.refineHypothesis(new DefaultQuery<>(Word.fromList(trace), output)));

        long duration = (System.nanoTime() - start) / NANOS_PER_MILLISECOND;
        LOGGER.info(Category.EVENT,
                    "Passed learner integration test {} ... took [{}]",
                    getTestName(),
                    String.format("%d.%03ds", duration / MILLIS_PER_SECOND, duration % MILLIS_PER_SECOND));
    }

    @Override
    public String getTestName() {
        return variant.getLearnerName() + "[" + variant.getName() + "]/" + example.getClass().getSimpleName();
    }

    protected abstract Word<I> checkEquivalence(M hypothesis);

}
