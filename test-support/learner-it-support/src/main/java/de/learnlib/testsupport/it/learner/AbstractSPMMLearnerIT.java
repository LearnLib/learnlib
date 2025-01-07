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

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.spmm.SimulatorEQOracle;
import de.learnlib.oracle.membership.SPMMSimulatorOracle;
import de.learnlib.testsupport.example.LearningExample.SPMMLearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPMMLearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SPMMLearnerVariantListImpl;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.automaton.procedural.SPMM;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for {@link SPMM} learning algorithms.
 */
public abstract class AbstractSPMMLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<SPMMLearningExample<?, ?>> examples = LearningExamples.createSPMMExamples();
        final List<AbstractLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>();

        for (SPMMLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<SPMMLearnerITCase<I, O>> createAllVariantsITCase(SPMMLearningExample<I, O> example) {

        final SPMM<?, I, ?, O> reference = example.getReferenceAutomaton();
        final MealyMembershipOracle<I, O> mqOracle = new SPMMSimulatorOracle<>(reference);
        final SPMMLearnerVariantListImpl<I, O> variants = new SPMMLearnerVariantListImpl<>();
        addLearnerVariants(example.getAlphabet(), reference.getErrorOutput(), mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example, variants, new SimulatorEQOracle<>(reference));
    }

    /**
     * Adds, for a given setup, all the variants of the DFA learner to be tested to the specified
     * {@link LearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param errorOutput
     *         the erroneous output symbol
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     */
    protected abstract <I, O> void addLearnerVariants(ProceduralInputAlphabet<I> alphabet,
                                                      O errorOutput,
                                                      MealyMembershipOracle<I, O> mqOracle,
                                                      SPMMLearnerVariantList<I, O> variants);
}
