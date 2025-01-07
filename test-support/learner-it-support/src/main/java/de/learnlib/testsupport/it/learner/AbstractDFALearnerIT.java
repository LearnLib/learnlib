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
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.membership.DFASimulatorOracle;
import de.learnlib.testsupport.example.LearningExample.DFALearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.DFALearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for DFA learning algorithms.
 * <p>
 * If run, this integration test tests the functionality of all
 * {@link #addLearnerVariants(Alphabet, int, DFAMembershipOracle, DFALearnerVariantList) variants} of a DFA learning
 * algorithm against all the examples contained in {@link LearningExamples#createDFAExamples()}.
 */
public abstract class AbstractDFALearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<DFALearningExample<?>> examples = LearningExamples.createDFAExamples();
        final List<UniversalDeterministicLearnerITCase<?, ?, ?>> result = new ArrayList<>();

        for (DFALearningExample<?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I> List<UniversalDeterministicLearnerITCase<I, Boolean, DFA<?, I>>> createAllVariantsITCase(
            DFALearningExample<I> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final DFAMembershipOracle<I> mqOracle = new DFASimulatorOracle<>(example.getReferenceAutomaton());
        final DFALearnerVariantListImpl<I> variants = new DFALearnerVariantListImpl<>();
        addLearnerVariants(alphabet, example.getReferenceAutomaton().size(), mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants,
                                                  new SimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    /**
     * Adds, for a given setup, all the variants of the DFA learner to be tested to the specified
     * {@link LearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param targetSize
     *         the size of the target automaton
     * @param mqOracle
     *         the membership oracle
     * @param variants
     *         list to add the learner variants to
     * @param <I>
     *         input symbol type
     */
    protected abstract <I> void addLearnerVariants(Alphabet<I> alphabet,
                                                   int targetSize,
                                                   DFAMembershipOracle<I> mqOracle,
                                                   DFALearnerVariantList<I> variants);
}
