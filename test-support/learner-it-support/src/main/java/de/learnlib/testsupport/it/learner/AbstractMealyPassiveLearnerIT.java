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
import de.learnlib.testsupport.example.DefaultPassiveLearningExample.DefaultMealyPassiveLearningExample;
import de.learnlib.testsupport.example.LearningExample.MealyLearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.example.PassiveLearningExample.MealyPassiveLearningExample;
import de.learnlib.testsupport.it.learner.PassiveLearnerVariantListImpl.MealyLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.word.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for passive Mealy machine learning algorithms.
 * <p>
 * Mealy machine learning algorithms tested by this integration test are expected to assume membership queries yield the
 * full output word corresponding to the suffix part of the query.
 */
public abstract class AbstractMealyPassiveLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<MealyLearningExample<?, ?>> examples = LearningExamples.createMealyExamples();
        final List<PassiveLearnerVariantITCase<?, ?, ?>> result = new ArrayList<>();

        for (MealyLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<PassiveLearnerVariantITCase<I, Word<O>, MealyMachine<?, I, ?, O>>> createAllVariantsITCase(
            MealyLearningExample<I, O> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final MealyMachine<?, I, ?, O> reference = example.getReferenceAutomaton();

        Collection<DefaultQuery<I, Word<O>>> queries = LearnerITUtil.generateSamples(alphabet, reference);

        final MealyLearnerVariantListImpl<I, O> variants = new MealyLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, variants);

        final MealyPassiveLearningExample<I, O> effectiveExample = new DefaultMealyPassiveLearningExample<>(queries);

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
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     */
    protected abstract <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                                      PassiveLearnerVariantList<MealyMachine<?, I, ?, O>, I, Word<O>> variants);
}
