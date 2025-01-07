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
package de.learnlib.algorithm.procedural.spmm.it;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import de.learnlib.AccessSequenceTransformer;
import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.dhc.mealy.MealyDHC;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.adapter.mealy.KearnsVaziraniAdapterMealy;
import de.learnlib.algorithm.procedural.adapter.mealy.LLambdaAdapterMealy;
import de.learnlib.algorithm.procedural.adapter.mealy.LStarBaseAdapterMealy;
import de.learnlib.algorithm.procedural.adapter.mealy.ObservationPackAdapterMealy;
import de.learnlib.algorithm.procedural.adapter.mealy.RivestSchapireAdapterMealy;
import de.learnlib.algorithm.procedural.adapter.mealy.TTTAdapterMealy;
import de.learnlib.algorithm.procedural.adapter.mealy.TTTLambdaAdapterMealy;
import de.learnlib.algorithm.procedural.spmm.ATManager;
import de.learnlib.algorithm.procedural.spmm.SPMMLearner;
import de.learnlib.algorithm.procedural.spmm.manager.DefaultATManager;
import de.learnlib.algorithm.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.learner.AbstractSPMMLearnerIT;
import de.learnlib.testsupport.it.learner.LearnerVariantList.SPMMLearnerVariantList;
import net.automatalib.alphabet.ProceduralInputAlphabet;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.word.Word;

public class SPMMIT extends AbstractSPMMLearnerIT {

    @Override
    protected <I, O> void addLearnerVariants(ProceduralInputAlphabet<I> alphabet,
                                             O errorOutput,
                                             MealyMembershipOracle<I, O> mqOracle,
                                             SPMMLearnerVariantList<I, O> variants) {

        final Builder<I, O> builder = new Builder<>(alphabet, errorOutput, mqOracle, variants);

        builder.addLearnerVariant(KearnsVaziraniAdapterMealy::new);
        builder.addLearnerVariant(LStarBaseAdapterMealy::new);
        builder.addLearnerVariant(MealyDHC::new);
        builder.addLearnerVariant(ObservationPackAdapterMealy::new);
        builder.addLearnerVariant(LLambdaAdapterMealy::new);
        builder.addLearnerVariant(TTTLambdaAdapterMealy::new);
        builder.addLearnerVariant(RivestSchapireAdapterMealy::new);
        builder.addLearnerVariant(TTTAdapterMealy::new);
    }

    private static class Builder<I, O> {

        private final ProceduralInputAlphabet<I> alphabet;
        private final O errorOutput;
        private final MembershipOracle<I, Word<O>> mqOracle;
        private final SPMMLearnerVariantList<I, O> variants;
        private final List<BiFunction<ProceduralInputAlphabet<I>, O, ATManager<I, O>>> atProviders;

        Builder(ProceduralInputAlphabet<I> alphabet,
                O errorOutput,
                MembershipOracle<I, Word<O>> mqOracle,
                SPMMLearnerVariantList<I, O> variants) {
            this.alphabet = alphabet;
            this.errorOutput = errorOutput;
            this.mqOracle = mqOracle;
            this.variants = variants;
            this.atProviders = Arrays.asList(DefaultATManager::new, OptimizingATManager::new);
        }

        <L extends MealyLearner<SymbolWrapper<I>, O> & SupportsGrowingAlphabet<SymbolWrapper<I>> & AccessSequenceTransformer<SymbolWrapper<I>>> void addLearnerVariant(
                LearnerConstructor<L, SymbolWrapper<I>, Word<O>> provider) {

            for (BiFunction<ProceduralInputAlphabet<I>, O, ATManager<I, O>> atProvider : atProviders) {
                final SPMMLearner<I, O, L> learner = new SPMMLearner<>(alphabet,
                                                                       errorOutput,
                                                                       mqOracle,
                                                                       i -> provider,
                                                                       atProvider.apply(alphabet, errorOutput));
                final String name = String.format("adapter=%s,manager=%s", provider, atProvider);
                variants.addLearnerVariant(name, learner);
            }
        }
    }

}
