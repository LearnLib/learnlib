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
package de.learnlib.algorithm.lsharp.it;

import java.util.Random;

import de.learnlib.algorithm.lsharp.LSharpMealy;
import de.learnlib.algorithm.lsharp.LSharpMealyBuilder;
import de.learnlib.algorithm.lsharp.Rule2;
import de.learnlib.algorithm.lsharp.Rule3;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.MQ2AQWrapper;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import org.testng.annotations.Test;

@Test
public class LSharpMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet, int targetSize, MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {

        final LSharpMealyBuilder<I, O> builder = new LSharpMealyBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(new MQ2AQWrapper<>(mqOracle));

        for (Rule2 r2 : Rule2.values()) {
            builder.setRule2(r2);
            for (Rule3 r3 : Rule3.values()) {
                builder.setRule3(r3);
                builder.setRandom(new Random(42)); // we like our tests deterministic
                LSharpMealy<I, O> learner = builder.create();
                String name = String.format("rule2=%s,rule3=%s", r2, r3);
                variants.addLearnerVariant(name, learner);
            }
        }
    }

}
