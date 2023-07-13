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
package de.learnlib.algorithms.spa;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.learnlib.algorithms.spa.adapter.DiscriminationTreeAdapter;
import de.learnlib.algorithms.spa.adapter.KearnsVaziraniAdapter;
import de.learnlib.algorithms.spa.adapter.LStarBaseAdapter;
import de.learnlib.algorithms.spa.adapter.RivestSchapireAdapter;
import de.learnlib.algorithms.spa.adapter.TTTAdapter;
import de.learnlib.algorithms.spa.manager.DefaultATRManager;
import de.learnlib.algorithms.spa.manager.OptimizingATRManager;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearnerConstructor;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractSPALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPALearnerVariantList;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.words.SPAAlphabet;

public class SPAIT extends AbstractSPALearnerIT {

    @Override
    protected <I> void addLearnerVariants(SPAAlphabet<I> alphabet,
                                          MembershipOracle<I, Boolean> mqOracle,
                                          SPALearnerVariantList<I> variants) {

        final Builder<I> builder = new Builder<>(alphabet, mqOracle, variants);

        builder.addLearnerVariant(DiscriminationTreeAdapter::new);
        builder.addLearnerVariant(KearnsVaziraniAdapter::new);
        builder.addLearnerVariant(LStarBaseAdapter::new);
        builder.addLearnerVariant(RivestSchapireAdapter::new);
        builder.addLearnerVariant(TTTAdapter::new);
    }

    private static class Builder<I> {

        private final SPAAlphabet<I> alphabet;
        private final MembershipOracle<I, Boolean> mqOracle;
        private final SPALearnerVariantList<I> variants;
        private final List<Function<SPAAlphabet<I>, ATRManager<I>>> atrProviders;

        Builder(SPAAlphabet<I> alphabet, MembershipOracle<I, Boolean> mqOracle, SPALearnerVariantList<I> variants) {
            this.alphabet = alphabet;
            this.mqOracle = mqOracle;
            this.variants = variants;
            this.atrProviders = Arrays.asList(DefaultATRManager::new, OptimizingATRManager::new);
        }

        <L extends DFALearner<I> & SupportsGrowingAlphabet<I> & AccessSequenceTransformer<I>> void addLearnerVariant(
                LearnerConstructor<L, I, Boolean> provider) {

            for (Function<SPAAlphabet<I>, ATRManager<I>> atrProvider : atrProviders) {
                final SPALearner<I, L> learner =
                        new SPALearner<>(alphabet, mqOracle, (i) -> provider, atrProvider.apply(alphabet));
                final String name = String.format("adapter=%s,manager=%s", provider, atrProvider);
                variants.addLearnerVariant(name, learner);
            }
        }
    }

}
