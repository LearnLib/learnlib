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
package de.learnlib.algorithms.lstar.it;

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.moore.ClassicLStarMooreBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMooreSymLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.MooreSymLearnerVariantList;
import net.automatalib.words.Alphabet;

public class ClassicLStarMooreIT extends AbstractMooreSymLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MembershipOracle<I, O> mqOracle,
                                             MooreSymLearnerVariantList<I, O> variants) {

        ClassicLStarMooreBuilder<I, O> builder = new ClassicLStarMooreBuilder<>();
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
