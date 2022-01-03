/* Copyright (C) 2013-2022 TU Dortmund
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
import de.learnlib.examples.LearningExample.OneSEVPALearningExample;
import de.learnlib.examples.LearningExamples;
import de.learnlib.oracle.equivalence.vpda.SimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.OneSEVPALearnerVariantListImpl;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.VPDAlphabet;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for {@link OneSEVPA} learning algorithms.
 *
 * @author frohme
 */
public abstract class AbstractOneSEVPALearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<OneSEVPALearningExample<?>> examples = LearningExamples.createOneSEVPAExamples();
        final List<OneSEVPALearnerITCase<?>> result = new ArrayList<>(examples.size());

        for (OneSEVPALearningExample<?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I> List<OneSEVPALearnerITCase<I>> createAllVariantsITCase(OneSEVPALearningExample<I> example) {

        final VPDAlphabet<I> alphabet = example.getAlphabet();
        final MembershipOracle<I, Boolean> mqOracle = new SimulatorOracle<>(example.getReferenceAutomaton());
        final OneSEVPALearnerVariantListImpl<I> variants = new OneSEVPALearnerVariantListImpl<>();
        addLearnerVariants(alphabet, mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants,
                                                  new SimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    /**
     * Adds, for a given setup, all the variants of the OneSEVPA learner to be tested to the specified {@link
     * LearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I> void addLearnerVariants(VPDAlphabet<I> alphabet,
                                                   MembershipOracle<I, Boolean> mqOracle,
                                                   LearnerVariantList.OneSEVPALearnerVariantList<I> variants);
}
