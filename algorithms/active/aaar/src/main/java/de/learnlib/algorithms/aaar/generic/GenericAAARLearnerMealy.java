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
package de.learnlib.algorithms.aaar.generic;

import java.util.function.Function;

import de.learnlib.algorithms.aaar.TranslatingMealyMachine;
import de.learnlib.api.algorithm.LearnerConstructor;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.words.Word;

/**
 * A {@link MealyMachine}-specific refinement of {@link AbstractGenericAAARLearner}.
 *
 * @param <L>
 *         learner type
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author frohme
 */
public class GenericAAARLearnerMealy<L extends MealyLearner<CI, O> & SupportsGrowingAlphabet<CI>, AI, CI, O>
        extends AbstractGenericAAARLearner<L, MealyMachine<?, AI, ?, O>, MealyMachine<?, CI, ?, O>, AI, CI, Word<O>> {

    /**
     * Constructor.
     *
     * @param learnerConstructor
     *         the provider for constructing the internal (concrete) learner
     * @param oracle
     *         the (concrete) membership oracle
     * @param initialConcrete
     *         the initial (concrete) input symbol used for starting the learning process
     * @param abstractor
     *         the function for creating new abstract input symbols given concrete one. This function only receives
     *         input symbols from the provided (concrete) counterexamples
     */
    public GenericAAARLearnerMealy(LearnerConstructor<L, CI, Word<O>> learnerConstructor,
                                   MembershipOracle<CI, Word<O>> oracle,
                                   CI initialConcrete,
                                   Function<CI, AI> abstractor) {
        super(learnerConstructor, oracle, initialConcrete, abstractor);
    }

    @Override
    public MealyMachine<?, AI, ?, O> getHypothesisModel() {
        final MealyMachine<?, CI, ?, O> concrete = super.getLearnerHypothesisModel();
        final CompactMealy<AI, O> result = new CompactMealy<>(getAbstractAlphabet());

        super.copyAbstract(concrete, result);

        return result;
    }

    @Override
    public MealyMachine<?, CI, ?, O> getTranslatingHypothesisModel() {
        return new TranslatingMealyMachine<>(super.getLearnerHypothesisModel(), this::getTreeForRepresentative);
    }
}

