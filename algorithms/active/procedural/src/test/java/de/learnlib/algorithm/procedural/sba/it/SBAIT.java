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
package de.learnlib.algorithm.procedural.sba.it;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.acex.AbstractNamedAcexAnalyzer;
import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.adapter.dfa.KearnsVaziraniAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.LLambdaAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.LStarBaseAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.ObservationPackAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.RivestSchapireAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.TTTAdapterDFA;
import de.learnlib.algorithm.procedural.adapter.dfa.TTTLambdaAdapterDFA;
import de.learnlib.algorithm.procedural.sba.ATManager;
import de.learnlib.algorithm.procedural.sba.SBALearner;
import de.learnlib.algorithm.procedural.sba.manager.DefaultATManager;
import de.learnlib.algorithm.procedural.sba.manager.OptimizingATManager;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractSBALearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SBALearnerVariantList;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;

public class SBAIT extends AbstractSBALearnerIT {

    @Override
    protected <I> void addLearnerVariants(ProceduralInputAlphabet<I> alphabet,
                                          DFAMembershipOracle<I> mqOracle,
                                          SBALearnerVariantList<I> variants) {

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
        private final SBALearnerVariantList<I> variants;
        private final List<Function<ProceduralInputAlphabet<I>, ATManager<I>>> atProviders;

        Builder(ProceduralInputAlphabet<I> alphabet,
                MembershipOracle<I, Boolean> mqOracle,
                SBALearnerVariantList<I> variants) {
            this.alphabet = alphabet;
            this.mqOracle = mqOracle;
            this.variants = variants;
            this.atProviders = Arrays.asList(DefaultATManager::new, OptimizingATManager::new);
        }

        <L extends DFALearner<SymbolWrapper<I>> & SupportsGrowingAlphabet<SymbolWrapper<I>> & AccessSequenceTransformer<SymbolWrapper<I>>> void addLearnerVariant(
                LearnerConstructor<L, SymbolWrapper<I>, Boolean> provider) {

            for (AbstractNamedAcexAnalyzer analyzer : AcexAnalyzers.getAllAnalyzers()) {
                for (Function<ProceduralInputAlphabet<I>, ATManager<I>> atProvider : atProviders) {
                    final SBALearner<I, L> learner =
                            new SBALearner<>(alphabet, mqOracle, i -> provider, analyzer, atProvider.apply(alphabet));
                    final String name =
                            String.format("adapter=%s,analyzer=%s,manager=%s", provider, analyzer, atProvider);
                    variants.addLearnerVariant(name, learner);
                }
            }
        }
    }

}
