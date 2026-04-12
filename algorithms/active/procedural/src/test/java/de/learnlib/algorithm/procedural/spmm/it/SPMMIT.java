/* Copyright (C) 2013-2026 TU Dortmund University
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
import de.learnlib.algorithm.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithm.lambda.lstar.LLambdaMealy;
import de.learnlib.algorithm.lambda.ttt.mealy.TTTLambdaMealy;
import de.learnlib.algorithm.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithm.observationpack.mealy.OPLearnerMealy;
import de.learnlib.algorithm.procedural.SymbolWrapper;
import de.learnlib.algorithm.procedural.spmm.ATManager;
import de.learnlib.algorithm.procedural.spmm.SPMMLearner;
import de.learnlib.algorithm.procedural.spmm.manager.DefaultATManager;
import de.learnlib.algorithm.procedural.spmm.manager.OptimizingATManager;
import de.learnlib.algorithm.rivestschapire.RivestSchapireMealy;
import de.learnlib.algorithm.ttt.mealy.TTTLearnerMealy;
import de.learnlib.oracle.MembershipOracle;
import de.learnlib.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.testsupport.it.AbstractSPMMLearnerIT;
import de.learnlib.testsupport.it.variant.LearnerVariantList.SPMMLearnerVariantList;
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

        builder.addLearnerVariant(KearnsVaziraniMealy::new);
        builder.addLearnerVariant(ExtensibleLStarMealy::new);
        builder.addLearnerVariant(MealyDHC::new);
        builder.addLearnerVariant(OPLearnerMealy::new);
        builder.addLearnerVariant(LLambdaMealy::new);
        builder.addLearnerVariant(TTTLambdaMealy::new);
        builder.addLearnerVariant(RivestSchapireMealy::new);
        builder.addLearnerVariant(TTTLearnerMealy::new);
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
