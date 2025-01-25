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
package de.learnlib.algorithm.kv.mealy;

import de.learnlib.acex.AbstractNamedAcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import org.testng.annotations.Test;

/**
 * Function test for the Kearns/Vazirani algorithm for Mealy machine learning.
 */
@Test
public class KearnsVaziraniMealyIT extends AbstractMealyLearnerIT {

    private static final boolean[] BOOLEAN_VALUES = {false, true};

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {
        KearnsVaziraniMealyBuilder<I, O> builder = new KearnsVaziraniMealyBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (boolean repeatedEval : BOOLEAN_VALUES) {
            builder.setRepeatedCounterexampleEvaluation(repeatedEval);
            for (AbstractNamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
                builder.setCounterexampleAnalyzer(analyzer);
                String name = String.format("repeatedEval=%s,ceAnalyzer=%s", repeatedEval, analyzer.getName());
                variants.addLearnerVariant(name, builder.create());
            }
        }
    }

}
