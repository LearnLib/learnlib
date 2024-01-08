/* Copyright (C) 2013-2024 TU Dortmund University
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
package de.learnlib.algorithm.aaar.explicit;

import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.algorithm.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithm.aaar.TranslatingMealyMachine;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.automaton.transducer.impl.CompactMealy;
import net.automatalib.word.Word;

/**
 * A {@link MealyMachine}-specific refinement of {@link AbstractExplicitAAARLearner}.
 *
 * @param <L>
 *         learner type
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 * @param <O>
 *         output symbol type
 */
public class ExplicitAAARLearnerMealy<L extends MealyLearner<CI, O> & SupportsGrowingAlphabet<CI>, AI, CI, O>
        extends AbstractExplicitAAARLearner<L, MealyMachine<?, AI, ?, O>, MealyMachine<?, CI, ?, O>, AI, CI, Word<O>> {

    /**
     * Constructor.
     *
     * @param learnerConstructor
     *         the provider for constructing the internal (concrete) learner
     * @param oracle
     *         the (concrete) membership oracle
     * @param explicitInitialAbstraction
     *         the initial mapping between concrete and abstract input symbols
     * @param incrementor
     *         the function for creating new abstract input symbols given concrete one. This function only receives
     *         input symbols from the provided explicitInitialAbstraction
     */
    public ExplicitAAARLearnerMealy(LearnerConstructor<L, CI, Word<O>> learnerConstructor,
                                    MembershipOracle<CI, Word<O>> oracle,
                                    ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                    Function<AI, AI> incrementor) {
        super(learnerConstructor, oracle, explicitInitialAbstraction, incrementor);
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

