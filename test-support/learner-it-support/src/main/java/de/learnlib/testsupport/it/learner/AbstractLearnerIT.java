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
import java.util.List;

import de.learnlib.examples.LearningExample;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.SuffixOutput;

/**
 * Abstract integration test for a learning algorithm (or "learner").
 * <p>
 * A learner integration test tests the functionality of a learning algorithm against a well-defined set of example
 * setups.
 * <p>
 * This class most probably does not need to be subclassed directly. Instead, extend one of the existing subclasses.
 *
 * @author Malte Isberner
 */
public abstract class AbstractLearnerIT {

    /**
     * Creates a list of per-example test cases for all learner variants.
     *
     * @return the list of test cases, one for each example
     */
    protected <I, D, A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & SuffixOutput<I, D>> List<LearnerVariantITCase<I, D, A>> createExampleITCases(
            LearningExample<I, D, A> example,
            LearnerVariantListImpl<A, I, D> variants) {

        final List<LearnerVariant<A, I, D>> variantList = variants.getLearnerVariants();
        final List<LearnerVariantITCase<I, D, A>> result = new ArrayList<>(variantList.size());

        for (LearnerVariant<A, I, D> variant : variantList) {
            result.add(new LearnerVariantITCase<>(variant, example));
        }

        return result;
    }

}
