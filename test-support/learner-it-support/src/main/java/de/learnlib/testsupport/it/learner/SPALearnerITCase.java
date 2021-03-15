/* Copyright (C) 2013-2021 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
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

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.spa.SimulatorEQOracle;
import net.automatalib.automata.spa.SPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.SPAAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

public class SPALearnerITCase<I> implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPALearnerITCase.class);

    private static final long NANOS_PER_MILLISECOND = 1_000_000;
    private static final long MILLIS_PER_SECOND = 1_000;

    private final LearnerVariant<SPA<?, I>, I, Boolean> variant;
    private final SPA<?, I> reference;
    private final SPAAlphabet<I> alphabet;

    SPALearnerITCase(LearnerVariant<SPA<?, I>, I, Boolean> variant, SPA<?, I> reference, SPAAlphabet<I> alphabet) {
        this.variant = variant;
        this.reference = reference;
        this.alphabet = alphabet;
    }

    @Test
    public void testLearning() {
        final EquivalenceOracle<SPA<?, I>, I, Boolean> eqOracle = new SimulatorEQOracle<>(reference);
        final LearningAlgorithm<? extends SPA<?, I>, I, Boolean> learner = variant.getLearner();

        final long start = System.nanoTime();

        learner.startLearning();
        DefaultQuery<I, Boolean> ceQuery;

        while ((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
            boolean refined = learner.refineHypothesis(ceQuery);
            Assert.assertTrue(refined, "Real counterexample " + ceQuery.getInput() + " did not refine hypothesis");
        }

        SPA<?, I> hypothesis = learner.getHypothesisModel();
        Assert.assertEquals(hypothesis.size(), reference.size());
        Assert.assertTrue(Automata.testEquivalence(reference, hypothesis, alphabet),
                          "Final hypothesis does not match reference automaton");

        long duration = (System.nanoTime() - start) / NANOS_PER_MILLISECOND;
        LOGGER.info("Passed learner integration test {} ... took [{}]",
                    getTestName(),
                    String.format("%d.%03ds", duration / MILLIS_PER_SECOND, duration % MILLIS_PER_SECOND));
    }

    @Override
    public String getTestName() {
        return variant.getLearnerName() + "[" + variant.getName() + "]";
    }

}
