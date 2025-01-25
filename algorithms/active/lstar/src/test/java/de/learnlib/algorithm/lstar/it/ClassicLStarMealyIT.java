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
package de.learnlib.algorithm.lstar.it;

import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithm.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithm.lstar.closing.ClosingStrategies;
import de.learnlib.algorithm.lstar.closing.ClosingStrategy;
import de.learnlib.algorithm.lstar.mealy.ClassicLStarMealyBuilder;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealySymLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealySymLearnerVariantList;
import net.automatalib.alphabet.Alphabet;

public class ClassicLStarMealyIT extends AbstractMealySymLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MembershipOracle<I, O> mqOracle,
                                             MealySymLearnerVariantList<I, O> variants) {

        ClassicLStarMealyBuilder<I, O> builder = new ClassicLStarMealyBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (ObservationTableCEXHandler<? super I, ? super O> handler : ObservationTableCEXHandlers.values()) {
            builder.setCexHandler(handler);
            for (ClosingStrategy<? super I, ? super O> closingStrategy : ClosingStrategies.values()) {
                builder.setClosingStrategy(closingStrategy);

                String variantName = "cexHandler=" + handler + ",closingStrategy=" + closingStrategy;
                variants.addLearnerVariant(variantName, builder.create());
            }
        }
    }

}
