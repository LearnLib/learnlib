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
package de.learnlib.algorithms.adt.it;

import java.util.Arrays;
import java.util.List;

import de.learnlib.algorithms.adt.api.ADTExtender;
import de.learnlib.algorithms.adt.api.LeafSplitter;
import de.learnlib.algorithms.adt.api.SubtreeReplacer;
import de.learnlib.algorithms.adt.config.ADTExtenders;
import de.learnlib.algorithms.adt.config.LeafSplitters;
import de.learnlib.algorithms.adt.config.SubtreeReplacers;
import de.learnlib.algorithms.adt.learner.ADTLearnerBuilder;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractMealyLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList;
import net.automatalib.words.Alphabet;

/**
 * @author frohme
 */
public class ADTIT extends AbstractMealyLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(Alphabet<I> alphabet,
                                             MembershipOracle.MealyMembershipOracle<I, O> mqOracle,
                                             LearnerVariantList.MealyLearnerVariantList<I, O> variants) {

        final ADTLearnerBuilder<I, O> builder = new ADTLearnerBuilder<>();
        builder.setAlphabet(alphabet);
        builder.setOracle(new MQ2SQWrapper<>(mqOracle));

        final List<LeafSplitter> leafSplitters =
                Arrays.asList(LeafSplitters.DEFAULT_SPLITTER, LeafSplitters.EXTEND_PARENT);
        final List<ADTExtender> adtExtenders = Arrays.asList(ADTExtenders.NOP, ADTExtenders.EXTEND_BEST_EFFORT);
        final List<SubtreeReplacer> subtreeReplacers = Arrays.asList(SubtreeReplacers.NEVER_REPLACE,
                                                                     SubtreeReplacers.EXHAUSTIVE_BEST_EFFORT,
                                                                     SubtreeReplacers.LEVELED_BEST_EFFORT,
                                                                     SubtreeReplacers.LEVELED_MIN_LENGTH,
                                                                     SubtreeReplacers.LEVELED_MIN_SIZE,
                                                                     SubtreeReplacers.SINGLE_BEST_EFFORT);

        for (int i = 0; i < leafSplitters.size(); i++) {
            final LeafSplitter leafSplitter = leafSplitters.get(i);
            builder.setLeafSplitter(leafSplitter);

            for (int j = 0; j < adtExtenders.size(); j++) {
                final ADTExtender adtExtender = adtExtenders.get(j);
                builder.setAdtExtender(adtExtender);

                for (int k = 0; k < subtreeReplacers.size(); k++) {
                    final SubtreeReplacer subtreeReplacer = subtreeReplacers.get(k);
                    builder.setSubtreeReplacer(subtreeReplacer);

                    variants.addLearnerVariant(i + "," + j + "," + k, builder.create());
                }
            }
        }
    }

}
