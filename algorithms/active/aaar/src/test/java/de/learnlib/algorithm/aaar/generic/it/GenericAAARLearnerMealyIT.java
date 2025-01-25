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
package de.learnlib.algorithm.aaar.generic.it;

import java.util.function.Function;

import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.aaar.AAARTestUtil;
import de.learnlib.algorithm.aaar.ComboConstructor;
import de.learnlib.algorithm.aaar.TranslatingLearnerWrapper;
import de.learnlib.algorithm.aaar.generic.GenericAAARLearnerMealy;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.common.util.Pair;
import net.automatalib.word.Word;

public class GenericAAARLearnerMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             int targetSize,
                                             MealyMembershipOracle<I, O> mqo,
                                             MealyLearnerVariantList<I, O> variants) {

        final int maxRounds = alphabet.size() + targetSize;
        final I firstSym = alphabet.getSymbol(0);

        for (Pair<String, ComboConstructor<? extends MealyLearner<I, O>, I, Word<O>>> l : AAARTestUtil.<I, O>getMealyLearners()) {
            final String name = l.getFirst();
            final ComboConstructor<? extends MealyLearner<I, O>, I, Word<O>> learner = l.getSecond();

            variants.addLearnerVariant(name,
                                       new TranslatingLearnerWrapper<>(new GenericAAARLearnerMealy<>(learner,
                                                                                                     mqo,
                                                                                                     firstSym,
                                                                                                     Function.identity())),
                                       maxRounds);
        }
    }
}
