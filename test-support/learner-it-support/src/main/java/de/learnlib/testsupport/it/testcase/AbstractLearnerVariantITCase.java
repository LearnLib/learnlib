/* Copyright (C) 2013-2026 TU Dortmund University
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
package de.learnlib.testsupport.it.testcase;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.algorithm.LearningAlgorithm;
import de.learnlib.logging.Category;
import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.LearningExample;
import de.learnlib.testsupport.it.util.LockableOracle;
import de.learnlib.testsupport.it.variant.LearnerVariant;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.concept.FiniteRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

public abstract class AbstractLearnerVariantITCase<I, D, M extends FiniteRepresentation> implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLearnerVariantITCase.class);

    private static final long NANOS_PER_MILLISECOND = 1_000_000;
    private static final long MILLIS_PER_SECOND = 1_000;

    private final LearnerVariant<? extends M, I, D> variant;
    private final LearningExample<I, ? extends M> example;
    private final LockableOracle<I, D> lockableOracle;
    private final EquivalenceOracle<? super M, I, D> eqOracle;

    AbstractLearnerVariantITCase(LearnerVariant<? extends M, I, D> variant,
                                 LearningExample<I, ? extends M> example,
                                 LockableOracle<I, D> lockableOracle,
                                 EquivalenceOracle<? super M, I, D> eqOracle) {
        this.variant = variant;
        this.example = example;
        this.lockableOracle = lockableOracle;
        this.eqOracle = eqOracle;
    }

    @Test
    public void testLearning() {
        lockableOracle.lock();
        LearningAlgorithm<? extends M, I, D> learner = variant.getLearner();

        Alphabet<I> alphabet = example.getAlphabet();
        M reference = example.getReferenceAutomaton();

        int maxRounds = variant.getMaxRounds();
        if (maxRounds < 0) {
            maxRounds = reference.size();
        }

        long start = System.nanoTime();

        lockableOracle.unlock();
        learner.startLearning();
        lockableOracle.lock();

        int roundCounter = 0;
        DefaultQuery<I, D> ceQuery;
        List<DefaultQuery<I, D>> ceQueries = new ArrayList<>();

        while ((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
            roundCounter++;
            if (roundCounter > maxRounds) {
                Assert.fail("Learning took too many rounds (> " + maxRounds + ")");
            }

            // this currently assumes a white-box equivalence oracle which does not pose any queries
            // for situations where this is not the case, the EQ may be given a non-lockable MQ
            lockableOracle.unlock();
            boolean refined = learner.refineHypothesis(ceQuery);
            lockableOracle.lock();

            Assert.assertTrue(refined, "Real counterexample " + ceQuery.getInput() + " did not refine hypothesis");
            ceQueries.add(ceQuery);
        }

        M hypothesis = learner.getHypothesisModel();
        Assert.assertTrue(testEquivalence(hypothesis), "Final hypothesis does not match reference automaton");

        if (hasCanonicalModel()) {
            Assert.assertEquals(hypothesis.size(), reference.size(), "Final hypothesis is not canonical");
        }

        if (!ceQueries.isEmpty()) {
            DefaultQuery<I, D> oldCe = ceQueries.get(0);
            Assert.assertFalse(learner.refineHypothesis(oldCe),
                               "Learner should not report a hypothesis update on outdated counterexample");
        }

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

    protected boolean hasCanonicalModel() {
        return true;
    }

    protected abstract boolean testEquivalence(M hypothesis);

}
