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

import de.learnlib.oracle.EquivalenceOracle.MooreEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.oracle.equivalence.MooreSimulatorEQOracle;
import de.learnlib.oracle.membership.MooreSimulatorOracle;
import de.learnlib.testsupport.example.LearningExample.MooreLearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MooreLearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.MooreLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for Moore machine learning algorithms.
 * <p>
 * Moore machine learning algorithms tested by this integration test are expected to assume membership queries yield the
 * full output word corresponding to the suffix part of the query.
 */
public abstract class AbstractMooreLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<MooreLearningExample<?, ?>> examples = LearningExamples.createMooreExamples();
        final List<UniversalDeterministicLearnerITCase<?, ?, ?>> result = new ArrayList<>();

        for (MooreLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<UniversalDeterministicLearnerITCase<I, Word<O>, MooreMachine<?, I, ?, O>>> createAllVariantsITCase(
            MooreLearningExample<I, O> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final MooreMachine<?, I, ?, O> reference = example.getReferenceAutomaton();
        final MooreMembershipOracle<I, O> mqOracle = new MooreSimulatorOracle<>(reference);
        final MooreEquivalenceOracle<I, O> eqOracle = new MooreSimulatorEQOracle<>(reference);
        final MooreLearnerVariantListImpl<I, O> variants = new MooreLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, reference.size(), mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example, variants, eqOracle);
    }

    /**
     * Adds, for a given setup, all the variants of the Moore machine learner to be tested to the specified
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
     * @param <O>
     *         output symbol type
     */
    protected abstract <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                                      int targetSize,
                                                      MooreMembershipOracle<I, O> mqOracle,
                                                      MooreLearnerVariantList<I, O> variants);
}
