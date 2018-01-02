/* Copyright (C) 2013-2018 TU Dortmund
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
import de.learnlib.examples.LearningExamples;
import de.learnlib.examples.PassiveLearningExample;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for passive Mealy machine learning algorithms.
 * <p>
 * Mealy machine learning algorithms tested by this integration test are expected to assume membership queries yield the
 * full output word corresponding to the suffix part of the query.
 *
 * @author frohme
 */
public abstract class AbstractMealyPassiveLearnerIT extends AbstractPassiveLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<PassiveLearnerVariantTICase<?, ?, ?>> result = new ArrayList<>();
        final List<LearningExample.MealyLearningExample<?, ?>> examples = LearningExamples.createMealyExamples();

        for (LearningExample.MealyLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<PassiveLearnerVariantTICase<I, Word<O>, MealyMachine<?, I, ?, O>>> createAllVariantsITCase(
            LearningExample.MealyLearningExample<I, O> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final MealyMachine<?, I, ?, O> reference = example.getReferenceAutomaton();

        Collection<DefaultQuery<I, Word<O>>> queries = super.generateSamples(alphabet, reference);

        final PassiveLearnerVariantListImpl<MealyMachine<?, I, ?, O>, I, Word<O>> variants =
                new PassiveLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, variants);

        final PassiveLearningExample<I, Word<O>> effectiveExample =
                new DefaultPassiveLearningExample<>(queries, alphabet);

        return super.createPassiveExampleITCases(effectiveExample, variants);
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
                                                      PassiveLearnerVariantList<MealyMachine<?, I, ?, O>, I, Word<O>> variants);
}