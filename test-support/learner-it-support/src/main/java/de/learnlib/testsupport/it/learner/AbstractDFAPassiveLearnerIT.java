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
import java.util.Collection;
import java.util.List;

import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.DefaultPassiveLearningExample.DefaultDFAPassiveLearningExample;
import de.learnlib.testsupport.example.LearningExample.DFALearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.example.PassiveLearningExample.DFAPassiveLearningExample;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantListImpl.DFAPassiveLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.fsa.DFA;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for passive DFA learning algorithms.
 * <p>
 * If run, this integration test tests the functionality of all {@link #addLearnerVariants(Alphabet,
 * PassiveLearnerVariantList) variants} of a DFA learning algorithm against all the examples contained in {@link
 * LearningExamples#createDFAExamples()}.
 * <p>
 * Subclasses can perform further sample filtering by overriding the {@link #generateSamplesInternal(Alphabet, DFA)}
 * method, e.g. if the learning algorithm is only capable of learning from positive examples.
 */
public abstract class AbstractDFAPassiveLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<DFALearningExample<?>> examples = LearningExamples.createDFAExamples();
        final List<PassiveLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>();

        for (DFALearningExample<?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I> List<PassiveLearnerVariantITCase<I, Boolean, DFA<?, I>>> createAllVariantsITCase(DFALearningExample<I> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final DFA<?, I> reference = example.getReferenceAutomaton();

        Collection<DefaultQuery<I, Boolean>> queries = generateSamplesInternal(alphabet, reference);

        final DFAPassiveLearnerVariantListImpl<I> variants = new DFAPassiveLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, variants);

        final DFAPassiveLearningExample<I> effectiveExample = new DefaultDFAPassiveLearningExample<>(queries);

        return LearnerITUtil.createPassiveExampleITCases(effectiveExample, variants);
    }

    /**
     * Utility method, that may be overriden by subclasses.
     *
     * @param alphabet
     *         the alphabet of the target system
     * @param reference
     *         the reference system from which samples will be drawn
     * @param <I>
     *         input symbol type
     *
     * @return the collection of generated queries used for learning
     */
    protected <I> Collection<DefaultQuery<I, Boolean>> generateSamplesInternal(Alphabet<I> alphabet,
                                                                               DFA<?, I> reference) {
        return LearnerITUtil.generateSamples(alphabet, reference);
    }

    /**
     * Adds, for a given setup, all the variants of the DFA learner to be tested to the specified {@link
     * PassiveLearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param variants
     *         list to add the learner variants to
     * @param <I>
     *         input symbol type
     */
    protected abstract <I> void addLearnerVariants(Alphabet<I> alphabet,
                                                   PassiveLearnerVariantList<DFA<?, I>, I, Boolean> variants);
}
