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

import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MooreMembershipOracle;
import de.learnlib.oracle.equivalence.SimulatorEQOracle;
import de.learnlib.oracle.membership.MooreSimulatorOracle;
import de.learnlib.testsupport.example.LearningExample.MooreLearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MooreSymLearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.MooreSymLearnerVariantListImpl;
import de.learnlib.util.moore.MooreUtil;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for Moore machine learning algorithms.
 * <p>
 * Mealy machine learning algorithms tested by this integration test are expected to assume membership queries yield
 * only the last symbol of the output word. If the learning algorithm expects the full output word for the suffix part
 * of the query, use {@link AbstractMooreLearnerIT}.
 */
public abstract class AbstractMooreSymLearnerIT {

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
        final MooreMembershipOracle<I, O> mqOracle = new MooreSimulatorOracle<>(example.getReferenceAutomaton());
        final MooreSymLearnerVariantListImpl<I, O> variants = new MooreSymLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, MooreUtil.wrapWordOracle(mqOracle), variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants.getMooreLearnerVariants(),
                                                  new SimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    /**
     * Adds, for a given setup, all the variants of the Mealy machine learner to be tested to the specified
     * {@link LearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
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
                                                      MembershipOracle<I, O> mqOracle,
                                                      MooreSymLearnerVariantList<I, O> variants);
}
