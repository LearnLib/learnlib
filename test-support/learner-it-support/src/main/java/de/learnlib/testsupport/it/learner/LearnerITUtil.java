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
import java.util.Random;

import de.learnlib.oracle.EquivalenceOracle;
import de.learnlib.query.DefaultQuery;
import de.learnlib.testsupport.example.LearningExample;
import de.learnlib.testsupport.example.LearningExample.OneSEVPALearningExample;
import de.learnlib.testsupport.example.LearningExample.SBALearningExample;
import de.learnlib.testsupport.example.LearningExample.SPALearningExample;
import de.learnlib.testsupport.example.LearningExample.SPMMLearningExample;
import de.learnlib.testsupport.example.LearningExample.UniversalDeterministicLearningExample;
import de.learnlib.testsupport.example.PassiveLearningExample;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.OneSEVPALearnerVariantListImpl;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SBALearnerVariantListImpl;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SPALearnerVariantListImpl;
import de.learnlib.testsupport.it.learner.LearnerVariantListImpl.SPMMLearnerVariantListImpl;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.UniversalAutomaton;
import net.automatalib.automaton.UniversalDeterministicAutomaton;
import net.automatalib.automaton.concept.FiniteRepresentation;
import net.automatalib.automaton.concept.Output;
import net.automatalib.automaton.concept.SuffixOutput;
import net.automatalib.automaton.procedural.SBA;
import net.automatalib.automaton.procedural.SPA;
import net.automatalib.automaton.procedural.SPMM;
import net.automatalib.automaton.vpa.OneSEVPA;
import net.automatalib.word.Word;
import net.automatalib.word.WordBuilder;

/**
 * Utility class for integration tests for a learning algorithm (or "learner").
 * <p>
 * A learner integration test tests the functionality of a learning algorithm against a well-defined set of example
 * setups.
 */
public final class LearnerITUtil {

    private static final int MAX_LENGTH = 50;
    private static final int MAX_SIZE = 100;

    private LearnerITUtil() {
        // prevent instantiation
    }

    /**
     * Creates a list of per-example test cases for all learner variants.
     *
     * @param example
     *         the example system
     * @param variants
     *         the list containing the various learner variants
     * @param eqOracle
     *         the equivalence oracle to use by the learning process
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     * @param <A>
     *         automaton type
     *
     * @return the list of test cases, one for each example
     */
    public static <I, D, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>> List<UniversalDeterministicLearnerITCase<I, D, A>> createExampleITCases(
            UniversalDeterministicLearningExample<I, ? extends A> example,
            LearnerVariantListImpl<A, I, D> variants,
            EquivalenceOracle<? super A, I, D> eqOracle) {
        // explicit generics are required for correct type-inference
        return LearnerITUtil.<I, D, A, UniversalDeterministicLearningExample<I, ? extends A>, UniversalDeterministicLearnerITCase<I, D, A>>createExampleITCasesInternal(
                example,
                variants,
                eqOracle,
                UniversalDeterministicLearnerITCase::new);
    }

    /**
     * Creates a list of per-example test cases for all learner variants.
     *
     * @param example
     *         the example system
     * @param variants
     *         the list containing the various learner variants
     * @param eqOracle
     *         the equivalence oracle to use by the learning process
     * @param <I>
     *         input symbol type
     *
     * @return the list of test cases, one for each example
     */
    public static <I> List<SPALearnerITCase<I>> createExampleITCases(SPALearningExample<I> example,
                                                                     SPALearnerVariantListImpl<I> variants,
                                                                     EquivalenceOracle<SPA<?, I>, I, Boolean> eqOracle) {
        // explicit generics are required for correct type-inference
        return LearnerITUtil.<I, Boolean, SPA<?, I>, SPALearningExample<I>, SPALearnerITCase<I>>createExampleITCasesInternal(
                example,
                variants,
                eqOracle,
                SPALearnerITCase::new);
    }

    /**
     * Creates a list of per-example test cases for all learner variants.
     *
     * @param example
     *         the example system
     * @param variants
     *         the list containing the various learner variants
     * @param eqOracle
     *         the equivalence oracle to use by the learning process
     * @param <I>
     *         input symbol type
     *
     * @return the list of test cases, one for each example
     */
    public static <I> List<SBALearnerITCase<I>> createExampleITCases(SBALearningExample<I> example,
                                                                     SBALearnerVariantListImpl<I> variants,
                                                                     EquivalenceOracle<SBA<?, I>, I, Boolean> eqOracle) {
        // explicit generics are required for correct type-inference
        return LearnerITUtil.<I, Boolean, SBA<?, I>, SBALearningExample<I>, SBALearnerITCase<I>>createExampleITCasesInternal(
                example,
                variants,
                eqOracle,
                SBALearnerITCase::new);
    }

    /**
     * Creates a list of per-example test cases for all learner variants.
     *
     * @param example
     *         the example system
     * @param variants
     *         the list containing the various learner variants
     * @param eqOracle
     *         the equivalence oracle to use by the learning process
     * @param <I>
     *         input symbol type
     * @param <O>
     *         output symbol type
     *
     * @return the list of test cases, one for each example
     */
    public static <I, O> List<SPMMLearnerITCase<I, O>> createExampleITCases(SPMMLearningExample<I, O> example,
                                                                            SPMMLearnerVariantListImpl<I, O> variants,
                                                                            EquivalenceOracle<SPMM<?, I, ?, O>, I, Word<O>> eqOracle) {
        // explicit generics are required for correct type-inference
        return LearnerITUtil.<I, Word<O>, SPMM<?, I, ?, O>, SPMMLearningExample<I, O>, SPMMLearnerITCase<I, O>>createExampleITCasesInternal(
                example,
                variants,
                eqOracle,
                SPMMLearnerITCase::new);
    }

    /**
     * Creates a list of per-example test cases for all learner variants.
     *
     * @param example
     *         the example system
     * @param variants
     *         the list containing the various learner variants
     * @param eqOracle
     *         the equivalence oracle to use by the learning process
     * @param <I>
     *         input symbol type
     *
     * @return the list of test cases, one for each example
     */
    public static <I> List<OneSEVPALearnerITCase<I>> createExampleITCases(OneSEVPALearningExample<I> example,
                                                                          OneSEVPALearnerVariantListImpl<I> variants,
                                                                          EquivalenceOracle<OneSEVPA<?, I>, I, Boolean> eqOracle) {
        // explicit generics are required for correct type-inference
        return LearnerITUtil.<I, Boolean, OneSEVPA<?, I>, OneSEVPALearningExample<I>, OneSEVPALearnerITCase<I>>createExampleITCasesInternal(
                example,
                variants,
                eqOracle,
                OneSEVPALearnerITCase::new);
    }

    private static <I, D, M extends FiniteRepresentation & Output<I, D>, L extends LearningExample<I, ? extends M>, C extends AbstractLearnerVariantITCase<I, D, M>> List<C> createExampleITCasesInternal(
            L example,
            LearnerVariantListImpl<M, I, D> variants,
            EquivalenceOracle<? super M, I, D> eqOracle,
            ITCaseBuilder<I, D, M, L, C> builder) {

        final List<LearnerVariant<M, I, D>> variantList = variants.getLearnerVariants();
        final List<C> result = new ArrayList<>(variantList.size());

        for (LearnerVariant<M, I, D> variant : variantList) {
            result.add(builder.build(variant, example, eqOracle));
        }

        return result;
    }

    /**
     * Creates a list of per-example test cases for all learner variants (passive version).
     *
     * @param example
     *         the example system
     * @param variants
     *         the list containing the various learner variants
     * @param <I>
     *         input symbol type
     * @param <D>
     *         output domain type
     * @param <A>
     *         automaton type
     *
     * @return the list of test cases, one for each example
     */
    public static <I, D, A extends SuffixOutput<I, D>> List<PassiveLearnerVariantITCase<I, D, A>> createPassiveExampleITCases(
            PassiveLearningExample<I, D> example,
            PassiveLearnerVariantListImpl<A, I, D> variants) {

        final List<PassiveLearnerVariant<A, I, D>> variantList = variants.getLearnerVariants();
        final List<PassiveLearnerVariantITCase<I, D, A>> result = new ArrayList<>(variantList.size());

        for (PassiveLearnerVariant<A, I, D> variant : variantList) {
            result.add(new PassiveLearnerVariantITCase<>(variant, example));
        }

        return result;
    }

    public static <I, D, M extends UniversalAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>> Collection<DefaultQuery<I, D>> generateSamples(
            Alphabet<I> alphabet,
            M reference) {

        final Random r = new Random(0);
        final int maxLength = Math.min(reference.size() * 2, MAX_LENGTH);
        final int size = Math.min(reference.size() * 2, MAX_SIZE);

        List<DefaultQuery<I, D>> result = new ArrayList<>(size);
        int alphabetSize = alphabet.size();

        for (int i = 0; i < size; i++) {
            int len = r.nextInt(maxLength + 1);
            WordBuilder<I> wb = new WordBuilder<>();
            for (int j = 0; j < len; j++) {
                wb.add(alphabet.getSymbol(r.nextInt(alphabetSize)));
            }
            Word<I> input = wb.toWord();
            D output = reference.computeOutput(input);
            DefaultQuery<I, D> qry = new DefaultQuery<>(input, output);
            result.add(qry);
        }
        return result;

    }

    @FunctionalInterface
    private interface ITCaseBuilder<I, D, M extends FiniteRepresentation & Output<I, D>, L extends LearningExample<I, ? extends M>, C extends AbstractLearnerVariantITCase<I, D, M>> {

        C build(LearnerVariant<M, I, D> variant, L example, EquivalenceOracle<? super M, I, D> eqOracle);
    }
}
