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
import java.util.Collection;
import java.util.List;

import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.DefaultPassiveLearningExample;
import de.learnlib.examples.LearningExample;
import de.learnlib.examples.LearningExample.MealyLearningExample;
import de.learnlib.examples.LearningExample.SSTLearningExample;
import de.learnlib.examples.LearningExamples;
import de.learnlib.examples.PassiveLearningExample;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.automata.transducers.SubsequentialTransducer;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for passive {@link SubsequentialTransducer}s learning algorithms.
 * <p>
 * SST learning algorithms tested by this integration test are expected to assume membership queries yield the full
 * output word corresponding to the suffix part of the query.
 *
 * @author frohme
 */
public abstract class AbstractSSTPassiveLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<PassiveLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>();

        for (MealyLearningExample<?, ?> example : LearningExamples.createMealyExamples()) {
            result.addAll(createAllVariantsITCase(example));
        }

        for (SSTLearningExample<?, ?> example : LearningExamples.createSSTExamples()) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, Word<O>>> List<PassiveLearnerVariantITCase<I, Word<O>, SubsequentialTransducer<?, I, ?, O>>> createAllVariantsITCase(
            LearningExample<I, A> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final A reference = example.getReferenceAutomaton();

        Collection<DefaultQuery<I, Word<O>>> queries = LearnerITUtil.generateSamples(alphabet, reference);

        final PassiveLearnerVariantListImpl<SubsequentialTransducer<?, I, ?, O>, I, Word<O>> variants =
                new PassiveLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, variants);

        final PassiveLearningExample<I, Word<O>> effectiveExample =
                new DefaultPassiveLearningExample<>(queries, alphabet);

        return LearnerITUtil.createPassiveExampleITCases(effectiveExample, variants);
    }

    /**
     * Adds, for a given setup, all the variants of the DFA learner to be tested to the specified {@link
     * PassiveLearnerVariantList variant list}.
     *
     * @param alphabet
     *         the input alphabet
     * @param variants
     *         list to add the learner variants to
     */
    protected abstract <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                                      PassiveLearnerVariantList<SubsequentialTransducer<?, I, ?, O>, I, Word<O>> variants);
}