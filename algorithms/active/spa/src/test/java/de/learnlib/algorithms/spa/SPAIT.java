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
package de.learnlib.algorithms.spa;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.learnlib.algorithms.spa.adapter.DiscriminationTreeAdapter;
import de.learnlib.algorithms.spa.adapter.KearnsVaziraniAdapter;
import de.learnlib.algorithms.spa.adapter.LStarBaseAdapter;
import de.learnlib.algorithms.spa.adapter.RivestSchapireAdapter;
import de.learnlib.algorithms.spa.adapter.TTTAdapter;
import de.learnlib.algorithms.spa.manager.DefaultATRManager;
import de.learnlib.algorithms.spa.manager.OptimizingATRManager;
import de.learnlib.api.AccessSequenceTransformer;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractSPALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPALearnerVariantList;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.words.Alphabet;
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
                BiFunction<Alphabet<I>, MembershipOracle<I, Boolean>, L> adapter) {

            final LearnerProvider<I, L> adapterAsProvider = (p, alph, mqo) -> adapter.apply(alph, mqo);

            for (Function<SPAAlphabet<I>, ATRManager<I>> provider : atrProviders) {
                // cast is required by compiler
                final SPALearner<I, L> learner =
                        new SPALearner<>(alphabet, mqOracle, adapterAsProvider, provider.apply(alphabet));
                final String name = String.format("adapter=%s,provider=%s", adapter, provider);
                variants.addLearnerVariant(name, learner);
            }
        }
    }

}
