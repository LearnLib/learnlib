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

import de.learnlib.driver.simulator.StateLocalInputMealySimulatorSUL;
import de.learnlib.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.equivalence.mealy.StateLocalInputMealySimulatorEQOracle;
import de.learnlib.oracle.membership.MealySimulatorOracle;
import de.learnlib.oracle.membership.StateLocalInputSULOracle;
import de.learnlib.testsupport.example.LearningExample.MealyLearningExample;
import de.learnlib.testsupport.example.LearningExample.StateLocalInputMealyLearningExample;
import de.learnlib.testsupport.example.LearningExamples;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.MealyLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.StateLocalInputMealyMachine;
import net.automatalib.util.automaton.transducer.MealyFilter;
import net.automatalib.word.Word;
import org.testng.annotations.Factory;

/**
 * Abstract integration test for Mealy machine learning algorithms.
 * <p>
 * Mealy machine learning algorithms tested by this integration test are expected to assume membership queries yield the
 * full output word corresponding to the suffix part of the query. If the learning algorithm only expects the last
 * symbol as output, use {@link AbstractMealySymLearnerIT}.
 */
public abstract class AbstractMealyLearnerIT {

    @Factory
    public Object[] createExampleITCases() {
        final List<MealyLearningExample<?, ?>> examples = LearningExamples.createMealyExamples();
        final List<UniversalDeterministicLearnerITCase<?, ?, ?>> result = new ArrayList<>();

        for (MealyLearningExample<?, ?> example : examples) {
            result.addAll(createAllVariantsITCase(example));
        }

        for (StateLocalInputMealyLearningExample<?, ?> example : LearningExamples.createSLIMealyExamples()) {
            result.addAll(createPartialVariantsITCase(example));
        }

        return result.toArray();
    }

    private <I, O> List<UniversalDeterministicLearnerITCase<I, Word<O>, MealyMachine<?, I, ?, O>>> createAllVariantsITCase(
            MealyLearningExample<I, O> example) {

        final Alphabet<I> alphabet = example.getAlphabet();
        final MealyMachine<?, I, ?, O> reference = example.getReferenceAutomaton();
        final MealyMembershipOracle<I, O> mqOracle = new MealySimulatorOracle<>(reference);
        final MealyLearnerVariantListImpl<I, O> variants = new MealyLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, reference.size(), mqOracle, variants);

        return LearnerITUtil.createExampleITCases(example,
                                                  variants,
                                                  new MealySimulatorEQOracle<>(example.getReferenceAutomaton()));
    }

    private <I, O> List<UniversalDeterministicLearnerITCase<I, Word<O>, MealyMachine<?, I, ?, O>>> createPartialVariantsITCase(
            StateLocalInputMealyLearningExample<I, O> example) {

        final StateLocalInputMealyMachine<?, I, ?, O> reference = example.getReferenceAutomaton();
        final Alphabet<I> alphabet = example.getAlphabet();
        final O undefinedOutput = example.getUndefinedOutput();

        // make sure, our oracle actually receives a partial mealy
        final StateLocalInputMealyMachine<?, I, ?, O> partialRef =
                MealyFilter.pruneTransitionsWithOutput(reference, alphabet, undefinedOutput);

        final MealyMembershipOracle<I, O> mqOracle =
                new StateLocalInputSULOracle<>(new StateLocalInputMealySimulatorSUL<>(partialRef), undefinedOutput);
        final MealyLearnerVariantListImpl<I, O> variants = new MealyLearnerVariantListImpl<>();
        addLearnerVariants(alphabet, reference.size(), mqOracle, variants);

        final MealyEquivalenceOracle<I, O> eqOracle =
                new StateLocalInputMealySimulatorEQOracle<>(partialRef, alphabet, undefinedOutput);

        return LearnerITUtil.createExampleITCases(example, variants, eqOracle);
    }

    /**
     * Adds, for a given setup, all the variants of the Mealy machine learner to be tested to the specified
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
                                                      MealyMembershipOracle<I, O> mqOracle,
                                                      MealyLearnerVariantList<I, O> variants);
}
