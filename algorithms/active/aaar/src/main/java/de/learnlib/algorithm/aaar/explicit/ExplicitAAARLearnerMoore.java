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
package de.learnlib.algorithm.aaar.explicit;

import java.util.function.Function;

import de.learnlib.algorithm.LearnerConstructor;
import de.learnlib.algorithm.LearningAlgorithm.MooreLearner;
import de.learnlib.algorithm.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithm.aaar.TranslatingMooreMachine;
import de.learnlib.oracle.MembershipOracle;
import net.automatalib.alphabet.SupportsGrowingAlphabet;
import net.automatalib.automaton.transducer.MooreMachine;
import net.automatalib.automaton.transducer.impl.CompactMoore;
import net.automatalib.word.Word;

/**
 * A {@link MooreLearner}-specific refinement of {@link AbstractExplicitAAARLearner}.
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
public class ExplicitAAARLearnerMoore<L extends MooreLearner<CI, O> & SupportsGrowingAlphabet<CI>, AI, CI, O>
        extends AbstractExplicitAAARLearner<L, MooreMachine<?, AI, ?, O>, MooreMachine<?, CI, ?, O>, AI, CI, Word<O>> {

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
    public ExplicitAAARLearnerMoore(LearnerConstructor<L, CI, Word<O>> learnerConstructor,
                                    MembershipOracle<CI, Word<O>> oracle,
                                    ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                    Function<AI, AI> incrementor) {
        super(learnerConstructor, oracle, explicitInitialAbstraction, incrementor);
    }

    @Override
    public MooreMachine<?, AI, ?, O> getHypothesisModel() {
        final MooreMachine<?, CI, ?, O> concrete = super.getLearnerHypothesisModel();
        final CompactMoore<AI, O> result = new CompactMoore<>(getAbstractAlphabet());

        super.copyAbstract(concrete, result);

        return result;
    }

    @Override
    public MooreMachine<?, CI, ?, O> getTranslatingHypothesisModel() {
        return new TranslatingMooreMachine<>(super.getLearnerHypothesisModel(), this::getTreeForRepresentative);
    }
}

