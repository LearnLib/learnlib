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
package de.learnlib.algorithms.oml.lstar.mealy.it;

import de.learnlib.algorithms.oml.lstar.OptimalLStarMealy;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.words.Alphabet;
import org.testng.annotations.Test;

@Test
public class OptimalLStarMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {

        variants.addLearnerVariant("OptimalLStarMealy", new OptimalLStarMealy<>(alphabet, mqOracle));
    }

}
