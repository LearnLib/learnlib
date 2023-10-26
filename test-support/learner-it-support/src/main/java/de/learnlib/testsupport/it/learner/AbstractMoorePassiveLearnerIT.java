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
import java.util.Collection;
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.example.DefaultPassiveLearningExample.DefaultMoorePassiveLearningExample;
import de.learnlib.example.LearningExample.MooreLearningExample;
import de.learnlib.example.LearningExamples;
import de.learnlib.example.PassiveLearningExample.MoorePassiveLearningExample;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantListImpl.MooreLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.word.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for passive Moore machine learning algorithms.
 * <p>
 * Mealy machine learning algorithms tested by this integration test are expected to assume membership queries yield the
 * full output word corresponding to the suffix part of the query.
 */
public abstract class AbstractMoorePassiveLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<MooreLearningExample<?, ?>> examples = LearningExamples.createMooreExamples();
        final List<PassiveLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>(examples.size());

        for (MooreLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<PassiveLearnerVariantITCase<I, Word<O>, MooreMachine<?, I, ?, O>>> createAllVariantsITCase(
            MooreLearningExample<I, O> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final MooreMachine<?, I, ?, O> reference = example.getReferenceAutomaton();

        Collection<DefaultQuery<I, Word<O>>> queries = LearnerITUtil.generateSamples(alphabet, reference);

        final MooreLearnerVariantListImpl<I, O> variants = new MooreLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, variants);

        final MoorePassiveLearningExample<I, O> effectiveExample = new DefaultMoorePassiveLearningExample<>(queries);

        return LearnerITUtil.createPassiveExampleITCases(effectiveExample, variants);
    }

    /**
     * Adds, for a given setup, all the variants of the Moore learner to be tested to the specified
     * {@link PassiveLearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                                      PassiveLearnerVariantList<MooreMachine<?, I, ?, O>, I, Word<O>> variants);
}
