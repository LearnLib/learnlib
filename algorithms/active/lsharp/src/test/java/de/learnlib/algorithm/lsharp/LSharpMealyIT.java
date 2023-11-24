/* Copyright (C) 2013-2023 TU Dortmund
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
package de.learnlib.algorithm.lsharp;

import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import org.testng.annotations.Test;

/**
 * Function test for the Kearns/Vazirani algorithm for Mealy machine learning.
 */
@Test
public class LSharpMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet, int targetSize, MealyMembershipOracle<I, O> mqOracle,
            MealyLearnerVariantList<I, O> variants) {

        for (Rule2 r2 : Rule2.values()) {
            for (Rule3 r3 : Rule3.values()) {
                LSharpMealy<I, O> learner = new LSharpMealy<>(alphabet, mqOracle, r2, r3);
                String name = String.format("rule2=%s,rule3=%s", r2, r3);
                variants.addLearnerVariant(name, learner);
            }
        }
    }

}
