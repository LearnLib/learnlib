/* Copyright (C) 2013-2019 TU Dortmund
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
import java.util.Random;

import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.examples.LearningExample;
import de.learnlib.examples.PassiveLearningExample;
import net.automatalib.automata.UniversalAutomaton;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

/**
 * Utility class for integration tests for a learning algorithm (or "learner").
 * <p>
 * A learner integration test tests the functionality of a learning algorithm against a well-defined set of example
 * setups.
 *
 * @author Malte Isberner
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
     * @return the list of test cases, one for each example
     */
    public static <I, D, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?>> List<LearnerVariantITCase<I, D, A>> createExampleITCases(
            LearningExample<I, A> example,
            LearnerVariantListImpl<A, I, D> variants,
            EquivalenceOracle<? super A, I, D> eqOracle) {

        final List<LearnerVariant<A, I, D>> variantList = variants.getLearnerVariants();
        final List<LearnerVariantITCase<I, D, A>> result = new ArrayList<>(variantList.size());

        for (LearnerVariant<A, I, D> variant : variantList) {
            result.add(new LearnerVariantITCase<>(variant, example, eqOracle));
        }

        return result;
    }

    /**
     * Creates a list of per-example test cases for all learner variants (passive version).
     *
     * @return the list of test cases, one for each example
     */
    public static <I, D, A extends SuffixOutput<I, D>> List<PassiveLearnerVariantTICase<I, D, A>> createPassiveExampleITCases(
            PassiveLearningExample<I, D> example,
            PassiveLearnerVariantListImpl<A, I, D> variants) {

        final List<PassiveLearnerVariant<A, I, D>> variantList = variants.getLearnerVariants();
        final List<PassiveLearnerVariantTICase<I, D, A>> result = new ArrayList<>(variantList.size());

        for (PassiveLearnerVariant<A, I, D> variant : variantList) {
            result.add(new PassiveLearnerVariantTICase<>(variant, example));
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
}
