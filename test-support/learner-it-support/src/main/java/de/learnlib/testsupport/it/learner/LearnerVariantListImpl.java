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
package de.learnlib.testsupport.it.learner;

import java.util.ArrayList;
import java.util.List;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.vpda.OneSEVPA;
import net.automatalib.words.Word;

class LearnerVariantListImpl<M, I, D> implements LearnerVariantList<M, I, D> {

    private final List<LearnerVariant<M, I, D>> learnerVariants = new ArrayList<>();

    public List<LearnerVariant<M, I, D>> getLearnerVariants() {
        return learnerVariants;
    }

    @Override
    public void addLearnerVariant(String name, LearningAlgorithm<? extends M, I, D> learner) {
        addLearnerVariant(name, learner, -1);
    }

    @Override
    public void addLearnerVariant(String name, LearningAlgorithm<? extends M, I, D> learner, int maxRounds) {
        LearnerVariant<M, I, D> variant = new LearnerVariant<>(name, learner, maxRounds);
        learnerVariants.add(variant);
    }

    public static class DFALearnerVariantListImpl<I> extends LearnerVariantListImpl<DFA<?, I>, I, Boolean>
            implements DFALearnerVariantList<I> {}

    public static class MealyLearnerVariantListImpl<I, O>
            extends LearnerVariantListImpl<MealyMachine<?, I, ?, O>, I, Word<O>>
            implements MealyLearnerVariantList<I, O> {}

    public static class OneSEVPALearnerVariantListImpl<I> extends LearnerVariantListImpl<OneSEVPA<?, I>, I, Boolean>
            implements OneSEVPALearnerVariantList<I> {}

    public static class MealySymLearnerVariantListImpl<I, O> implements MealySymLearnerVariantList<I, O> {

        private final MealyLearnerVariantListImpl<I, O> mealyLearnerVariants = new MealyLearnerVariantListImpl<>();

        public MealyLearnerVariantListImpl<I, O> getMealyLearnerVariants() {
            return mealyLearnerVariants;
        }

        @Override
        public void addLearnerVariant(String name,
                                      LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, O> learner) {
            addLearnerVariant(name, learner, -1);
        }

        @Override
        public void addLearnerVariant(String name,
                                      LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, O> learner,
                                      int maxRounds) {
            mealyLearnerVariants.addLearnerVariant(name, MealyUtil.wrapSymbolLearner(learner), maxRounds);
        }

    }

}
