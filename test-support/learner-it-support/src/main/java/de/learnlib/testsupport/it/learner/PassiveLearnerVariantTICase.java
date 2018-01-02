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

import java.util.Collection;

import de.learnlib.api.algorithm.PassiveLearningAlgorithm;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.PassiveLearningExample;
import net.automatalib.automata.concepts.SuffixOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

/**
 * Default test case for a passive learning integration test.
 *
 * @param <I>
 *         input symbol type
 * @param <D>
 *         output domain type
 * @param <M>
 *         inferred model type
 *
 * @author frohme
 */
public final class PassiveLearnerVariantTICase<I, D, M extends SuffixOutput<I, D>> implements ITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PassiveLearnerVariantTICase.class);

    private static final long NANOS_PER_MILLISECOND = 1000000;
    private static final long MILLIS_PER_SECOND = 1000;

    private final PassiveLearnerVariant<? extends M, I, D> variant;
    private final PassiveLearningExample<I, D> example;

    PassiveLearnerVariantTICase(PassiveLearnerVariant<? extends M, I, D> variant,
                                PassiveLearningExample<I, D> example) {
        this.variant = variant;
        this.example = example;
    }

    @Test
    public void testLearning() {
        final PassiveLearningAlgorithm<? extends M, I, D> learner = variant.getLearner();
        final Collection<DefaultQuery<I, D>> queries = example.getSamples();

        learner.addSamples(example.getSamples());

        long start = System.nanoTime();
        final M model = learner.computeModel();

        for (DefaultQuery<I, D> qry : queries) {
            Assert.assertEquals(model.computeSuffixOutput(qry.getPrefix(), qry.getSuffix()), qry.getOutput());
        }

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
