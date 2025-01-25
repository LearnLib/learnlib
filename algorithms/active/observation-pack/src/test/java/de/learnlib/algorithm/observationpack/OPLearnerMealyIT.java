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
package de.learnlib.algorithm.observationpack;

import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealyBuilder;
import de.learnlib.counterexample.LocalSuffixFinder;
import de.learnlib.counterexample.LocalSuffixFinders;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.word.Word;
import org.testng.annotations.Test;

@Test
public class OPLearnerMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {
        OPLearnerMealyBuilder<I, O> builder = new OPLearnerMealyBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (LocalSuffixFinder<? super I, ? super Word<O>> suffixFinder : LocalSuffixFinders.values()) {
            builder.setSuffixFinder(suffixFinder);

            String name = "suffixFinder=" + suffixFinder.toString();
            variants.addLearnerVariant(name, builder.create());
        }
    }

}
