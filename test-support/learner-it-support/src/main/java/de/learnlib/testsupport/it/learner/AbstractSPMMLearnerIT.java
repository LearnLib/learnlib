/* Copyright (C) 2013-2023 TU Dortmund
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

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.examples.LearningExample.SPMMLearningExample;
import de.learnlib.examples.LearningExamples;
import de.learnlib.oracle.equivalence.spmm.SimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPMMLearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SPMMLearnerVariantListImpl;
import net.automatalib.words.ProceduralInputAlphabet;
import net.automatalib.words.ProceduralOutputAlphabet;
import net.automatalib.words.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for VPDA learning algorithms.
 *
 * @author frohme
 */
public abstract class AbstractSPMMLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<SPMMLearningExample<?, ?>> examples = LearningExamples.createSPMMExamples();
        final List<AbstractLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>(examples.size());

        for (SPMMLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<SPMMLearnerITCase<I, O>> createAllVariantsITCase(SPMMLearningExample<I, O> example) {

        final MembershipOracle<I, Word<O>> mqOracle = new SimulatorOracle<>(example.getReferenceAutomaton());
        final SPMMLearnerVariantListImpl<I, O> variants = new SPMMLearnerVariantListImpl<>();
        addLearnerVariants(example.getAlphabet(), example.getOutputAlphabet(), mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants,
                                                  new SimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    /**
     * Adds, for a given setup, all the variants of the DFA learner to be tested to the specified
     * {@link LearnerVariantList variant list}.
     *
     * @param inputAlphabet
     *         the input alphabet
     * @param outputAlphabet
     *         the output alphabet
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I, O> void addLearnerVariants(ProceduralInputAlphabet<I> inputAlphabet,
                                                      ProceduralOutputAlphabet<O> outputAlphabet,
                                                      MembershipOracle<I, Word<O>> mqOracle,
                                                      SPMMLearnerVariantList<I, O> variants);
}
