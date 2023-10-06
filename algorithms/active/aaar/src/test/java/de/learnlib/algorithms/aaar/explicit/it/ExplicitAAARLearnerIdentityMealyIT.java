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
package de.learnlib.algorithms.aaar.explicit.it;

import de.learnlib.algorithms.aaar.AAARTestUtil;
import de.learnlib.algorithms.aaar.ComboConstructor;
import de.learnlib.algorithms.aaar.TranslatingLearnerWrapper;
import de.learnlib.algorithms.aaar.explicit.ExplicitAAARLearnerMealy;
import de.learnlib.algorithms.aaar.explicit.IdentityInitialAbstraction;
import de.learnlib.algorithms.aaar.explicit.NoopIncrementor;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.commons.util.Pair;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class ExplicitAAARLearnerIdentityMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqo,
                                             MealyLearnerVariantList<I, O> variants) {

        for (Pair<String, ComboConstructor<? extends MealyLearner<I, O>, I, Word<O>>> l : AAARTestUtil.<I, O>getMealyLearners()) {
            final String name = l.getFirst();
            final ComboConstructor<? extends MealyLearner<I, O>, I, Word<O>> learner = l.getSecond();

            variants.addLearnerVariant(name,
                                       new TranslatingLearnerWrapper<>(new ExplicitAAARLearnerMealy<>(learner,
                                                                                                      mqo,
                                                                                                      new IdentityInitialAbstraction<>(
                                                                                                              alphabet),
                                                                                                      new NoopIncrementor<>())));
        }
    }
}
