/* Copyright (C) 2013-2018 TU Dortmund
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
import de.learnlib.oracle.equivalence.vpda.SimulatorEQOracle;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.VPDAlphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

public class VPDALearnerITCase<I> implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VPDALearnerITCase.class);

    private static final long NANOS_PER_MILLISECOND = 1000000;
    private static final long MILLIS_PER_SECOND = 1000;

    private final LearnerVariant<OneSEVPA<?, I>, I, Boolean> variant;
    private final OneSEVPA<?, I> reference;
    private final VPDAlphabet<I> alphabet;

    VPDALearnerITCase(LearnerVariant<OneSEVPA<?, I>, I, Boolean> variant,
                      OneSEVPA<?, I> reference,
                      VPDAlphabet<I> alphabet) {
        this.variant = variant;
        this.reference = reference;
        this.alphabet = alphabet;
    }

    @Test
    public void testLearning() {
        final EquivalenceOracle<OneSEVPA<?, I>, I, Boolean> eqOracle = new SimulatorEQOracle<>(reference, alphabet);
        final LearningAlgorithm<? extends OneSEVPA<?, I>, I, Boolean> learner = variant.getLearner();

        final long start = System.nanoTime();

        learner.startLearning();
        DefaultQuery<I, Boolean> ceQuery;

        while ((ceQuery = eqOracle.findCounterExample(learner.getHypothesisModel(), alphabet)) != null) {
            boolean refined = learner.refineHypothesis(ceQuery);
            Assert.assertTrue(refined, "Real counterexample " + ceQuery.getInput() + " did not refine hypothesis");
        }

        Assert.assertTrue(Automata.testEquivalence(reference, learner.getHypothesisModel(), alphabet),
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
