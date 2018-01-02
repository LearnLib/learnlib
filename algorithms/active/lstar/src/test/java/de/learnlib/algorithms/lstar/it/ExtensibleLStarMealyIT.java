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
package de.learnlib.algorithms.lstar.it;

import java.util.Arrays;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MealyLearnerVariantList;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class ExtensibleLStarMealyIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             MealyLearnerVariantList<I, O> variants) {
        ExtensibleLStarMealyBuilder<I, O> builder = new ExtensibleLStarMealyBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        builder.setInitialPrefixes(Arrays.asList(Word.epsilon(), Word.fromLetter(alphabet.getSymbol(0))));

        for (ObservationTableCEXHandler<? super I, ? super Word<O>> handler : ObservationTableCEXHandlers.values()) {
            builder.setCexHandler(handler);
            for (ClosingStrategy<? super I, ? super Word<O>> closingStrategy : ClosingStrategies.values()) {
                builder.setClosingStrategy(closingStrategy);

                String variantName = "cexHandler=" + handler + ",closingStrategy=" + closingStrategy;
                variants.addLearnerVariant(variantName, builder.create());
            }
        }
    }

}
