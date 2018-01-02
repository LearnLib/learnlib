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
import de.learnlib.examples.LearningExample;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

final class LearnerVariantITCase<I, D, M extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>>
        implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LearnerVariantITCase.class);

    private static final long NANOS_PER_MILLISECOND = 1000000;
    private static final long MILLIS_PER_SECOND = 1000;

    private final LearnerVariant<? extends M, I, D> variant;
    private final LearningExample<I, D, ? extends M> example;

    LearnerVariantITCase(LearnerVariant<? extends M, I, D> variant, LearningExample<I, D, ? extends M> example) {
        this.variant = variant;
        this.example = example;
    }

    @Test
    public void testLearning() {
        LearningAlgorithm<? extends M, I, D> learner = variant.getLearner();

        Alphabet<I> alphabet = example.getAlphabet();

        int maxRounds = variant.getMaxRounds();
        if (maxRounds < 0) {
            maxRounds = example.getReferenceAutomaton().size();
        }

        EquivalenceOracle<? super M, I, D> eqOracle = new SimulatorEQOracle<>(example.getReferenceAutomaton());

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

        Assert.assertNull(Automata.findSeparatingWord(example.getReferenceAutomaton(),
                                                      learner.getHypothesisModel(),
                                                      alphabet), "Final hypothesis does not match reference automaton");

        long duration = (System.nanoTime() - start) / NANOS_PER_MILLISECOND;
        LOGGER.info("Passed learner integration test {} ... took [{}]",
                    getTestName(),
                    String.format("%d.%03ds", duration / MILLIS_PER_SECOND, duration % MILLIS_PER_SECOND));
    }

    @Override
    public String getTestName() {
        return variant.getLearnerName() + "[" + variant.getName() + "]/" + example.getClass().getSimpleName();
    }

}
