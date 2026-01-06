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
package de.learnlib.testsupport.it;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.learnlib.driver.simulator.MMLTSimulatorSUL;
import de.learnlib.oracle.TimedQueryOracle;
import de.learnlib.oracle.equivalence.mmlt.SimulatorEQOracle;
import de.learnlib.oracle.membership.TimedSULOracle;
import de.learnlib.testsupport.example.LearningExample.MMLTLearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.testcase.AbstractLearnerVariantITCase;
import de.learnlib.testsupport.it.testcase.MMLTLearnerITCase;
import de.learnlib.testsupport.it.util.LearnerITUtil;
import de.learnlib.testsupport.it.util.MMLTLockableOracle;
import de.learnlib.testsupport.it.variant.LearnerVariantList;
import de.learnlib.testsupport.it.variant.LearnerVariantList.MMLTLearnerVariantList;
import de.learnlib.testsupport.it.variant.LearnerVariantListImpl.MMLTLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.mmlt.MMLT;
import org.testng.annotations.Factory;

public abstract class AbstractMMLTLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<MMLTLearningExample<?, ?>> examples = LearningExamples.createMMLTExamples();
        final List<MMLTLearningExample<?, ?>> extras = getAdditionalLearningExamples();
        final List<AbstractLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>();

        for (MMLTLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }
        for (MMLTLearningExample<?, ?> example : extras) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<MMLTLearnerITCase<I, O>> createAllVariantsITCase(MMLTLearningExample<I, O> example) {

        final Alphabet<I> alphabet = example.getUntimedAlphabet();
        final TimedQueryOracle<I, O> simOracle = new TimedSULOracle<>(new MMLTSimulatorSUL<>(example.getReferenceAutomaton()), example.getParams());
        final MMLTLockableOracle<I, O> mqOracle = new MMLTLockableOracle<>(simOracle);
        final MMLTLearnerVariantListImpl<I, O> variants = new MMLTLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, mqOracle, example, variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants,
                                                  mqOracle,
                                                  new SimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    protected List<MMLTLearningExample<?, ?>> getAdditionalLearningExamples() {
        return Collections.emptyList();
    }

    /**
     * Adds, for a given setup, all the variants of the {@link MMLT} learner to be tested to the specified
     * {@link LearnerVariantList variant list}.
     *
     * @param <I>
     *         input symbol type (of non-delaying inputs)
     * @param <O>
     *         output symbol type
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param example
     *         the learning example to potentially extract additional information
     * @param variants
     *         the list to add the learner variants to
     */
    protected abstract <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                                      TimedQueryOracle<I, O> mqOracle,
                                                      MMLTLearningExample<I, O> example,
                                                      MMLTLearnerVariantList<I, O> variants);
}
