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
package de.learnlib.algorithms.aaar.explicit;

import java.util.function.Function;

import de.learnlib.algorithms.aaar.ExplicitInitialAbstraction;
import de.learnlib.algorithms.aaar.TranslatingDFA;
import de.learnlib.api.algorithm.LearnerConstructor;
import de.learnlib.api.algorithm.LearningAlgorithm.DFALearner;
import de.learnlib.api.oracle.MembershipOracle;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

/**
 * A {@link DFA}-specific refinement of {@link AbstractExplicitAAARLearner}.
 *
 * @param <L>
 *         learner type
 * @param <AI>
 *         abstract input symbol type
 * @param <CI>
 *         concrete input symbol type
 *
 * @author frohme
 */
public class ExplicitAAARLearnerDFA<L extends DFALearner<CI> & SupportsGrowingAlphabet<CI>, AI, CI>
        extends AbstractExplicitAAARLearner<L, DFA<?, AI>, DFA<?, CI>, AI, CI, Boolean> {

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
    public ExplicitAAARLearnerDFA(LearnerConstructor<L, CI, Boolean> learnerConstructor,
                                  MembershipOracle<CI, Boolean> oracle,
                                  ExplicitInitialAbstraction<AI, CI> explicitInitialAbstraction,
                                  Function<AI, AI> incrementor) {
        super(learnerConstructor, oracle, explicitInitialAbstraction, incrementor);
    }

    @Override
    public DFA<?, AI> getHypothesisModel() {
        final DFA<?, CI> hyp = super.getLearnerHypothesisModel();
        final CompactDFA<AI> result = new CompactDFA<>(getAbstractAlphabet());

        super.copyAbstract(hyp, result);

        return result;
    }

    @Override
    public DFA<?, CI> getTranslatingHypothesisModel() {
        return new TranslatingDFA<>(super.getLearnerHypothesisModel(), this::getTreeForRepresentative);
    }

}

