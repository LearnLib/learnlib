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

import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.dfa.ExtensibleLStarDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractDFALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.DFALearnerVariantList;
import net.automatalib.words.Alphabet;

public class ExtensibleLStarDFAIT extends AbstractDFALearnerIT {

    @Override
    protected <I> void addLearnerVariants(Alphabet<I> alphabet,
                                          int targetSize,
                                          DFAMembershipOracle<I> mqOracle,
                                          DFALearnerVariantList<I> variants) {
        ExtensibleLStarDFABuilder<I> builder = new ExtensibleLStarDFABuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(mqOracle);

        for (ObservationTableCEXHandler<? super I, ? super Boolean> handler : ObservationTableCEXHandlers.values()) {
            builder.setCexHandler(handler);
            for (ClosingStrategy<? super I, ? super Boolean> closingStrategy : ClosingStrategies.values()) {
                builder.setClosingStrategy(closingStrategy);

                String variantName = "cexHandler=" + handler + ",closingStrategy=" + closingStrategy;
                variants.addLearnerVariant(variantName, builder.create());
            }
        }
    }

}
