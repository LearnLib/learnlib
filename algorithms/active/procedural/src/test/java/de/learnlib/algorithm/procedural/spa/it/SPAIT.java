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
package de.learnlib.algorithm.procedural.spa.it;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.acex.AbstractNamedAcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.procedural.adapter.dfa.KearnsVaziraniAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.LLambdaAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.LStarBaseAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.ObservationPackAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.RivestSchapireAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.TTTAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.TTTLambdaAdapterDFA;
import de.learnlib.algorithm.procedural.spa.ATRManager;
import de.learnlib.algorithm.procedural.spa.SPALearner;
import de.learnlib.algorithm.procedural.spa.manager.DefaultATRManager;
import de.learnlib.algorithm.procedural.spa.manager.OptimizingATRManager;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractSPALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPALearnerVariantList;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;

public class SPAIT extends AbstractSPALearnerIT {

    @Override
    protected <I> void addLearnerVariants(ProceduralInputAlphabet<I> alphabet,
                                          DFAMembershipOracle<I> mqOracle,
                                          SPALearnerVariantList<I> variants) {

        final Builder<I> builder = new Builder<>(alphabet, mqOracle, variants);

        builder.addLearnerVariant(KearnsVaziraniAdapterDFA::new);
        builder.addLearnerVariant(LStarBaseAdapterDFA::new);
        builder.addLearnerVariant(ObservationPackAdapterDFA::new);
        builder.addLearnerVariant(LLambdaAdapterDFA::new);
        builder.addLearnerVariant(TTTLambdaAdapterDFA::new);
        builder.addLearnerVariant(RivestSchapireAdapterDFA::new);
        builder.addLearnerVariant(TTTAdapterDFA::new);
    }

    private static class Builder<I> {

        private final ProceduralInputAlphabet<I> alphabet;
        private final MembershipOracle<I, Boolean> mqOracle;
        private final SPALearnerVariantList<I> variants;
        private final List<Function<ProceduralInputAlphabet<I>, ATRManager<I>>> atrProviders;

        Builder(ProceduralInputAlphabet<I> alphabet,
                MembershipOracle<I, Boolean> mqOracle,
                SPALearnerVariantList<I> variants) {
            this.alphabet = alphabet;
            this.mqOracle = mqOracle;
            this.variants = variants;
            this.atrProviders = Arrays.asList(DefaultATRManager::new, OptimizingATRManager::new);
        }

        <L extends DFALearner<I> & SupportsGrowingAlphabet<I> & AccessSequenceTransformer<I>> void addLearnerVariant(
                LearnerConstructor<L, I, Boolean> provider) {

            for (AbstractNamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
                for (Function<ProceduralInputAlphabet<I>, ATRManager<I>> atrProvider : atrProviders) {
                    final SPALearner<I, L> learner =
                            new SPALearner<>(alphabet, mqOracle, i -> provider, analyzer, atrProvider.apply(alphabet));
                    final String name =
                            String.format("adapter=%s,analyzer=%s,manager=%s", provider, analyzer, atrProvider);
                    variants.addLearnerVariant(name, learner);
                }
            }
        }
    }

}
