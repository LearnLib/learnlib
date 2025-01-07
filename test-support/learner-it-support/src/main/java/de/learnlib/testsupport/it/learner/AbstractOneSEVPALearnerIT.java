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

import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.oracle.equivalence.vpa.SimulatorEQOracle;
import de.learnlib.oracle.membership.SEVPASimulatorOracle;
import de.learnlib.testsupport.example.LearningExample.OneSEVPALearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.OneSEVPALearnerVariantListImpl;
import net.automatalib.alphabet.VPAlphabet;
import net.automatalib.automaton.vpa.OneSEVPA;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for {@link OneSEVPA} learning algorithms.
 */
public abstract class AbstractOneSEVPALearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<OneSEVPALearningExample<?>> examples = LearningExamples.createOneSEVPAExamples();
        final List<OneSEVPALearnerITCase<?>> result = new ArrayList<>();

        for (OneSEVPALearningExample<?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I> List<OneSEVPALearnerITCase<I>> createAllVariantsITCase(OneSEVPALearningExample<I> example) {

        final VPAlphabet<I> alphabet = example.getAlphabet();
        final DFAMembershipOracle<I> mqOracle = new SEVPASimulatorOracle<>(example.getReferenceAutomaton());
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
     * @param <I>
     *         input symbol type
     */
    protected abstract <I> void addLearnerVariants(VPAlphabet<I> alphabet,
                                                   DFAMembershipOracle<I> mqOracle,
                                                   LearnerVariantList.OneSEVPALearnerVariantList<I> variants);
}
